package unicon.Achiva.domain.push;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.member.MemberService;
import unicon.Achiva.domain.push.dto.*;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.UUID;

/**
 * PushController
 * <p>
 * Expo Push Notification 관련 REST API를 제공합니다.
 * - LinkToken 발급 (Cognito JWT 필요)
 * - 푸시 토큰 등록 (LinkToken 검증, 공개 엔드포인트)
 * - 푸시 알림 전송 (Member 존재 확인 필요)
 */
@Slf4j
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final PushService pushService;
    private final AuthService authService;
    private final MemberService memberService;

    /**
     * LinkToken 발급
     * 로그인된 사용자만 호출 가능 (Cognito JWT 필요)
     * <p>
     * POST /api/push/link-intent
     * Authorization: Bearer {cognito_jwt}
     *
     * @return LinkTokenResponse (linkToken, expiresIn)
     */
    @Operation(summary = "푸시 토큰 연동용 LinkToken 발급 - Cognito JWT 필요")
    @PostMapping("/link-intent")
    public ResponseEntity<LinkTokenResponse> issueLinkToken() {
        UUID memberId = authService.getMemberIdFromToken();
        LinkTokenResponse response = pushService.generateLinkToken(memberId);

        log.info("[Push API] LinkToken 발급 - memberId: {}", memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 푸시 토큰 등록
     * linkToken으로 인증 (Cognito JWT 불필요)
     * <p>
     * POST /api/push/register
     * Content-Type: application/json
     * Body: {linkToken, expoPushToken, deviceInfo}
     *
     * @param request PushRegisterRequest
     * @return PushRegisterResponse (success, message)
     */
    @Operation(summary = "Expo 푸시 토큰 등록 - CognitoJWT 불필요")
    @PostMapping("/register")
    public ResponseEntity<PushRegisterResponse> registerPushToken(
            @Valid @RequestBody PushRegisterRequest request
    ) {
        PushRegisterResponse response = pushService.registerPushToken(request);

        log.info("[Push API] 푸시 토큰 등록 - expoPushToken: {}", request.getExpoPushToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 푸시 알림 전송
     * 로그인 + Member 존재 확인 필요
     * <p>
     * POST /api/push/send
     * Authorization: Bearer {cognito_jwt}
     * Content-Type: application/json
     * Body: {targetMemberId, title, body, data}
     *
     * @param request PushSendRequest
     * @return PushSendResponse (success, sentCount, failedCount, results)
     */
    @Operation(summary = "푸시 알림 전송 - targetMemberId null이면 전체 회원 발송, Cognito JWT 필요")
    @PostMapping("/send")
    public ResponseEntity<PushSendResponse> sendPushNotification(
            @Valid @RequestBody PushSendRequest request
    ) {
        UUID senderId = authService.getMemberIdFromToken();
        PushSendResponse response = pushService.sendPushNotification(senderId, request);

        log.info("[Push API] 푸시 전송 - sender: {}, target: {}, sent: {}, failed: {}",
                senderId, request.getTargetMemberId(), response.getSentCount(), response.getFailedCount());
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "푸시 알림 사용 여부 설정 (true: 허용, false: 비활성화)")
    @PutMapping("/members/me/push-enabled/{enabled}")
    public ResponseEntity<ApiResponseForm<Boolean>> updateMyPushEnabled(
            @PathVariable boolean enabled
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        memberService.updatePushEnabled(memberId, enabled);
        return ResponseEntity.ok(ApiResponseForm.success(
                enabled,
                "푸시 알림 사용 여부 변경 성공"
        ));
    }
}
