package unicon.Achiva.domain.push;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.domain.push.dto.ExpoPushResponse;
import unicon.Achiva.domain.push.dto.LinkTokenResponse;
import unicon.Achiva.domain.push.dto.PushRegisterRequest;
import unicon.Achiva.domain.push.dto.PushRegisterResponse;
import unicon.Achiva.domain.push.dto.PushSendRequest;
import unicon.Achiva.domain.push.dto.PushSendResponse;
import unicon.Achiva.domain.push.entity.LinkToken;
import unicon.Achiva.domain.push.entity.PushToken;
import unicon.Achiva.domain.push.infrastructure.ExpoPushClient;
import unicon.Achiva.domain.push.infrastructure.LinkTokenRepository;
import unicon.Achiva.domain.push.infrastructure.PushTokenRepository;
import unicon.Achiva.global.response.GeneralException;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushService {

    private final LinkTokenRepository linkTokenRepository;
    private final MemberRepository memberRepository;
    private final PushTokenRepository pushTokenRepository;
    private final ExpoPushClient expoPushClient;

    @Value("${app.security.link-token-secret}")
    private String linkTokenSecret;

    @Value("${app.security.link-token-expiry:300}")
    private int linkTokenExpiry;

    /**
     * LinkToken 생성
     * 로그인된 사용자를 위한 일회용 5분 유효 토큰 발급
     *
     * @param memberId 사용자 UUID
     * @return LinkTokenResponse (linkToken, expiresIn)
     */
    @Transactional
    public LinkTokenResponse generateLinkToken(UUID memberId) {
        // 1. Member 존재 확인
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. JWT 생성을 위한 시간 정보
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (linkTokenExpiry * 1000L));
        String jti = UUID.randomUUID().toString();

        // 3. JJWT로 linkToken 생성
        String linkToken = Jwts.builder()
            .setSubject(memberId.toString())
            .claim("purpose", "push-registration")
            .setIssuedAt(now)
            .setExpiration(expiry)
            .setId(jti)
            .signWith(
                Keys.hmacShaKeyFor(linkTokenSecret.getBytes()),
                SignatureAlgorithm.HS256
            )
            .compact();

        // 4. DB에 jti 저장 (1회용 보장)
        LinkToken storedToken = LinkToken.builder()
            .jti(jti)
            .memberId(memberId)
            .expiresAt(expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
            .used(false)
            .build();
        linkTokenRepository.save(storedToken);

        log.info("[Push] LinkToken 생성 - memberId: {}, jti: {}", memberId, jti);

        return LinkTokenResponse.builder()
            .linkToken(linkToken)
            .expiresIn(linkTokenExpiry)
            .build();
    }

    /**
     * LinkToken 검증 및 소비
     * JWT 서명, 만료, 1회용 검증 후 memberId 반환
     *
     * @param linkToken JWT 형식의 linkToken
     * @return UUID 사용자 ID
     * @throws GeneralException INVALID_LINK_TOKEN, EXPIRED_LINK_TOKEN, ALREADY_USED_LINK_TOKEN
     */
    @Transactional
    public UUID validateAndConsumeLinkToken(String linkToken) {
        try {
            // 1. JWT 파싱 및 서명 검증
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(linkTokenSecret.getBytes()))
                .build()
                .parseClaimsJws(linkToken)
                .getBody();

            // 2. purpose 확인
            String purpose = claims.get("purpose", String.class);
            if (!"push-registration".equals(purpose)) {
                log.warn("[Push] LinkToken purpose 불일치 - purpose: {}", purpose);
                throw new GeneralException(PushErrorCode.INVALID_LINK_TOKEN);
            }

            // 3. jti 확인 (1회용)
            String jti = claims.getId();
            LinkToken storedToken = linkTokenRepository.findByJti(jti)
                .orElseThrow(() -> {
                    log.warn("[Push] LinkToken DB에 존재하지 않음 - jti: {}", jti);
                    return new GeneralException(PushErrorCode.INVALID_LINK_TOKEN);
                });

            // 4. 이미 사용된 토큰인지 확인
            if (storedToken.isUsed()) {
                log.warn("[Push] 이미 사용된 LinkToken - jti: {}", jti);
                throw new GeneralException(PushErrorCode.ALREADY_USED_LINK_TOKEN);
            }

            // 5. 만료 확인 (JWT 자체 만료는 JwtException으로 처리됨)
            if (storedToken.isExpired()) {
                log.warn("[Push] 만료된 LinkToken - jti: {}, expiresAt: {}", jti, storedToken.getExpiresAt());
                throw new GeneralException(PushErrorCode.EXPIRED_LINK_TOKEN);
            }

            // 6. 사용 처리
            storedToken.markAsUsed();
            linkTokenRepository.save(storedToken);

            UUID memberId = storedToken.getMemberId();
            log.info("[Push] LinkToken 검증 성공 - jti: {}, memberId: {}", jti, memberId);

            return memberId;

        } catch (ExpiredJwtException e) {
            log.warn("[Push] LinkToken JWT 만료 - token: {}", linkToken);
            throw new GeneralException(PushErrorCode.EXPIRED_LINK_TOKEN);
        } catch (JwtException e) {
            log.warn("[Push] LinkToken JWT 검증 실패 - token: {}, error: {}", linkToken, e.getMessage());
            throw new GeneralException(PushErrorCode.INVALID_LINK_TOKEN);
        }
    }

    /**
     * 푸시 토큰 등록
     * linkToken을 검증하고 Expo Push Token을 사용자에게 매핑
     * 새로운 토큰 등록 시 기존 활성 토큰들을 자동으로 비활성화
     *
     * @param request PushRegisterRequest (linkToken, expoPushToken, deviceInfo)
     * @return PushRegisterResponse (success, message)
     */
    @Transactional
    public PushRegisterResponse registerPushToken(PushRegisterRequest request) {
        // 1. LinkToken 검증 및 memberId 추출
        UUID memberId = validateAndConsumeLinkToken(request.getLinkToken());

        // 2. Expo Push Token 형식 검증
        // 정상 형식: ExponentPushToken[영숫자_-] 또는 Expo[영숫자_-]
        String expoPushToken = request.getExpoPushToken();
        if (!expoPushToken.matches("^(ExponentPushToken|Expo)\\[[A-Za-z0-9_-]+\\]$")) {
            log.warn("[Push] 잘못된 Expo 토큰 형식 - token: {}", expoPushToken);
            throw new GeneralException(PushErrorCode.INVALID_EXPO_TOKEN);
        }

        // 3. 동일한 토큰이 이미 등록되어 있는지 확인
        Optional<PushToken> existing = pushTokenRepository
            .findByMemberIdAndExpoPushToken(memberId, expoPushToken);

        if (existing.isPresent()) {
            // 동일한 토큰이 이미 있으면 디바이스 정보만 갱신하고 활성화
            PushToken token = existing.get();
            if (request.getDeviceInfo() != null) {
                token.updateDeviceInfo(request.getDeviceInfo());
            }
            if (!token.isActive()) {
                token.reactivate();
            }
            pushTokenRepository.save(token);
            log.info("[Push] 기존 푸시 토큰 갱신 - memberId: {}, token: {}", memberId, expoPushToken);
        } else {
            // 4. 새로운 토큰이면 기존 활성 토큰들을 모두 비활성화
            List<PushToken> activeTokens = pushTokenRepository.findAllByMemberIdAndIsActiveTrue(memberId);
            if (!activeTokens.isEmpty()) {
                activeTokens.forEach(PushToken::deactivate);
                pushTokenRepository.saveAll(activeTokens);
                log.info("[Push] 기존 활성 토큰 비활성화 - memberId: {}, count: {}", memberId, activeTokens.size());
            }

            // 5. 신규 토큰 생성
            PushToken newToken = PushToken.builder()
                .memberId(memberId)
                .expoPushToken(expoPushToken)
                .deviceInfo(request.getDeviceInfo())
                .isActive(true)
                .build();
            pushTokenRepository.save(newToken);
            log.info("[Push] 푸시 토큰 신규 등록 - memberId: {}, token: {}", memberId, expoPushToken);
        }

        return PushRegisterResponse.builder()
            .success(true)
            .message("푸시 토큰이 등록되었습니다")
            .build();
    }

    /**
     * 푸시 알림 전송
     * 특정 사용자 또는 전체에게 푸시 알림 전송
     *
     * @param senderId 전송자 ID (로그용)
     * @param request PushSendRequest (targetMemberId, title, body, data)
     * @return PushSendResponse (success, sentCount, failedCount, results)
     */
    @Transactional
    public PushSendResponse sendPushNotification(UUID senderId, PushSendRequest request) {
        UUID targetMemberId = request.getTargetMemberId();

        // 1. 전송 대상 결정
        List<PushToken> tokens;
        if (targetMemberId == null) {
            // 전체 발송 (pushEnabled && isActive)
            tokens = pushTokenRepository.findAllActiveByPushEnabled();
            log.info("[Push] 전체 푸시 전송 시작 - sender: {}, targetCount: {}", senderId, tokens.size());
        } else {
            // 특정 사용자에게만
            Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new GeneralException(PushErrorCode.MEMBER_NOT_FOUND_FOR_PUSH));

            if (!target.isPushEnabled()) {
                log.warn("[Push] 푸시 비활성화 사용자 - memberId: {}", targetMemberId);
                throw new GeneralException(PushErrorCode.PUSH_DISABLED);
            }

            tokens = pushTokenRepository.findAllByMemberIdAndIsActiveTrue(targetMemberId);
            log.info("[Push] 개별 푸시 전송 시작 - sender: {}, target: {}, tokenCount: {}",
                     senderId, targetMemberId, tokens.size());
        }

        // 2. 전송 대상이 없으면 조기 리턴
        if (tokens.isEmpty()) {
            log.warn("[Push] 전송 대상 없음 - targetMemberId: {}", targetMemberId);
            return PushSendResponse.builder()
                .success(false)
                .sentCount(0)
                .failedCount(0)
                .message("전송 대상이 없습니다")
                .results(List.of())
                .build();
        }

        // 3. Expo API로 전송 (각 토큰별로)
        List<PushSendResponse.PushResult> results = tokens.stream()
            .map(token -> {
                try {
                    ExpoPushResponse response = expoPushClient.sendPushNotification(
                        token.getExpoPushToken(),
                        request.getTitle(),
                        request.getBody(),
                        request.getData()
                    );

                    // 성공 시 lastUsedAt 갱신
                    if ("ok".equals(response.getStatus())) {
                        token.updateLastUsedAt();
                        pushTokenRepository.save(token);
                    }

                    return PushSendResponse.PushResult.builder()
                        .expoPushToken(token.getExpoPushToken())
                        .status(response.getStatus())
                        .ticketId(response.getTicketId())
                        .message(response.getMessage())
                        .build();
                } catch (Exception e) {
                    log.error("[Push] 전송 실패 - token: {}, error: {}", token.getExpoPushToken(), e.getMessage());
                    return PushSendResponse.PushResult.builder()
                        .expoPushToken(token.getExpoPushToken())
                        .status("error")
                        .message(e.getMessage())
                        .build();
                }
            })
            .toList();

        // 4. 통계 계산
        long sentCount = results.stream().filter(r -> "ok".equals(r.getStatus())).count();
        long failedCount = results.size() - sentCount;

        log.info("[Push] 전송 완료 - sent: {}, failed: {}, total: {}", sentCount, failedCount, results.size());

        return PushSendResponse.builder()
            .success(sentCount > 0)
            .sentCount((int) sentCount)
            .failedCount((int) failedCount)
            .message(String.format("푸시 전송 완료 (성공: %d, 실패: %d)", sentCount, failedCount))
            .results(results)
            .build();
    }
}
