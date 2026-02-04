package unicon.Achiva.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.auth.dto.*;
import unicon.Achiva.domain.auth.infrastructure.CognitoService;
import unicon.Achiva.domain.auth.infrastructure.OIDCUserInfoService;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.member.Gender;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.dto.MemberResponse;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.global.response.GeneralException;
import unicon.Achiva.global.utill.NicknameGeneratorUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final OIDCUserInfoService oidcUserInfoService;
    private final CognitoService cognitoService;

    @Transactional
    public CreateMemberResponse signup(MemberRequest requestDto) {
        String email = getEmailFromToken().orElse(oidcUserInfoService.getEmailFromUserInfo().orElseThrow(() -> new GeneralException(MemberErrorCode.INVALID_TOKEN)));
        String nickName = determineNickname(email);
        boolean emailExists = memberRepository.existsByEmail(email);
        boolean nickNameExists = memberRepository.existsByNickName(nickName);

        if (emailExists) {
            throw new GeneralException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        if (nickNameExists) {
            if (isAppleUser() || isGoogleUser()) {
                do {
                    nickName = NicknameGeneratorUtil.generate();
                } while (memberRepository.existsByNickName(nickName));
            } else {
                throw new GeneralException(MemberErrorCode.DUPLICATE_NICKNAME);
            }
        }

        Member member = Member.builder()
                .id(getMemberIdFromToken())
                .email(email)
                .nickName(nickName)
                .profileImageUrl(requestDto.getProfileImageUrl() != null ? requestDto.getProfileImageUrl() : "https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png")
                .birth(requestDto.getBirth())
                .gender(requestDto.getGender() != null ? requestDto.getGender() : null)
                .region(requestDto.getRegion() != null ? requestDto.getRegion() : null)
                .categories(requestDto.getCategories())
                .role(Role.USER)
                .build();

        Member savedMember = memberRepository.save(member);


        return CreateMemberResponse.fromEntity(savedMember);
    }

    /**
     * Determines the nickname based on the following priority:
     * 1. If a nickname is present in the JWT token, use it.
     * 2. If the email is an Apple private relay address, generate a random nickname.
     * 3. Otherwise, use the part of the email before the "@" symbol.
     * 4. If all else fails, generate a random nickname.
     *
     * @param email the user's email address
     * @return the determined nickname
     */
    private String determineNickname(String email) {
        int atIndex = email.indexOf('@');
        boolean hasLocalPart = atIndex > 0;
        String lowerEmail = email.toLowerCase();

        if (isGoogleUser()) {
            return hasLocalPart
                    ? email.substring(0, atIndex)
                    : NicknameGeneratorUtil.generate();
        }

        if (isAppleUser()) {
            boolean isPrivateRelay = lowerEmail.endsWith("privaterelay.appleid.com");
            if (isPrivateRelay) {
                return NicknameGeneratorUtil.generate();
            }
            return hasLocalPart
                    ? email.substring(0, atIndex)
                    : NicknameGeneratorUtil.generate();
        }

        if (hasLocalPart) {
            return getUserNameFromToken().orElse(email.substring(0, atIndex));
        }

        return NicknameGeneratorUtil.generate();
    }

    /**
     * 부분 갱신(PATCH) 형태의 회원 정보 업데이트.
     * null 이 아닌 필드만 엔티티에 반영하며, 닉네임은 변경 시 중복 검증을 수행한다.
     *
     * @param memberId   업데이트할 회원 식별자
     * @param requestDto 변경 요청 DTO (null 허용 필드는 선택 적용)
     * @return 갱신된 회원 응답 DTO
     * @throws GeneralException MEMBER_NOT_FOUND: 회원 없음
     */
    @Transactional
    public MemberResponse updateMember(UUID memberId, UpdateMemberRequest requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        Optional.ofNullable(requestDto.getNickName())
                .filter(n -> !Objects.equals(n, member.getNickName()))
                .ifPresent(n -> {
                    validateDuplicateNickName(n);
                    member.updateNickName(n);
                });

        Optional.ofNullable(requestDto.getProfileImageUrl())
                .ifPresent(member::updateProfileImageUrl);

        Optional.ofNullable(requestDto.getBirth())
                .map(LocalDate::parse)
                .ifPresent(member::updateBirth);

        Optional.ofNullable(requestDto.getGender())
                .map(Gender::valueOf)
                .ifPresent(member::updateGender);

        Optional.ofNullable(requestDto.getRegion())
                .ifPresent(member::updateRegion);

        Optional.ofNullable(requestDto.getCategories())
                .map(list -> list.stream()
                        .map(Category::fromDisplayName)
                        .toList())
                .ifPresent(member::updateCategories);

        Optional.ofNullable(requestDto.getDescription())
                .ifPresent(member::updateDescription);

        return MemberResponse.fromEntity(member);
    }

    @Transactional
    public void deleteMember(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        var userName = getUserNameFromToken().orElseThrow(() -> new GeneralException(MemberErrorCode.INVALID_TOKEN));

        cognitoService.globalSignOut(userName);
        cognitoService.disableUser(userName);
        cognitoService.deleteUser(userName);
        memberRepository.delete(member);
    }

    public CheckEmailResponse validateDuplicateEmail(String email) {
        boolean isExists = memberRepository.existsByEmail(email);
        if (isExists) {
            throw new GeneralException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        return new CheckEmailResponse(true);
    }

    public CheckNicknameResponse validateDuplicateNickName(String nickName) {
        boolean isExists = memberRepository.existsByNickName(nickName);
        if (isExists) {
            throw new GeneralException(MemberErrorCode.DUPLICATE_NICKNAME);
        }
        return new CheckNicknameResponse(true);
    }

    /**
     * Extracts the memberId from the JWT subject ("sub") claim.
     *
     * @return the UUID parsed from the JWT subject claim
     * @throws GeneralException if authentication is missing, not a JwtAuthenticationToken,
     *                          or the subject is not a valid UUID
     */
    public UUID getMemberIdFromToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new GeneralException(MemberErrorCode.INVALID_TOKEN);
        }

        String sub = jwtAuth.getToken().getSubject();
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new GeneralException(MemberErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * Extracts the nickname from the JWT "username" claim.
     * If the user is authenticated via Apple or Google, returns Optional.empty().
     * Because social logins have "...signinwith{socialProvider}" usernames.
     *
     * @return an Optional containing the nickname if present and not a social login; otherwise, Optional.empty()
     * @throws GeneralException if authentication is missing or not a JwtAuthenticationToken
     */
    public Optional<String> getUserNameFromToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new GeneralException(MemberErrorCode.INVALID_TOKEN);
        }

        String nickname = jwtAuth.getToken().getClaimAsString("username");

        return Optional.ofNullable(nickname);
    }

    public Optional<String> getEmailFromToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new GeneralException(MemberErrorCode.INVALID_TOKEN);
        }

        String email = jwtAuth.getToken().getClaimAsString("email");

        return Optional.ofNullable(email);
    }

    private Boolean isSocialeUser(String socialProvider) {
        String _socialProvider = socialProvider.toLowerCase();
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new GeneralException(MemberErrorCode.INVALID_TOKEN);
        }

        // 방법 1: cognito:groups 확인
        ArrayList<String> cognitoGroups = jwtAuth.getToken().getClaim("cognito:groups");
        var isSocialProviderGroup = cognitoGroups != null && cognitoGroups.size() > 0 && cognitoGroups.stream()
                .map(String::toLowerCase)
                .anyMatch(group -> group.contains("signinwith" + _socialProvider));

        // 방법 2: username 패턴 확인 (Google/Apple 사용자는 "google_" 또는 "apple_"로 시작)
        String username = jwtAuth.getToken().getSubject();
        boolean usernameMatches = username != null && username.toLowerCase().startsWith(_socialProvider + "_");

        return isSocialProviderGroup || usernameMatches;
    }

    private Boolean isAppleUser() {
        return isSocialeUser("apple");
    }

    private Boolean isGoogleUser() {
        return isSocialeUser("google");
    }

    /**
     * 소셜 로그인(Google/Apple) 사용자를 자동으로 회원가입 처리합니다.
     * 최소한의 정보(email, nickname, 기본 프로필 이미지)만으로 Member를 생성합니다.
     *
     * @return 생성된 Member 또는 이미 존재하는 Member
     */
    @Transactional
    public Member autoSignupSocialUser() {
        UUID memberId = getMemberIdFromToken();

        // 이미 Member가 존재하면 반환
        if (memberRepository.existsById(memberId)) {
            return memberRepository.findById(memberId)
                    .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        }

        // 소셜 로그인 사용자가 아니면 예외
        if (!isGoogleUser() && !isAppleUser()) {
            throw new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        // 이메일 가져오기
        String email = getEmailFromToken()
                .orElse(oidcUserInfoService.getEmailFromUserInfo()
                        .orElseThrow(() -> new GeneralException(MemberErrorCode.INVALID_TOKEN)));

        // 닉네임 자동 생성
        String nickName = determineNickname(email);

        // 닉네임 중복 시 랜덤 생성
        while (memberRepository.existsByNickName(nickName)) {
            nickName = NicknameGeneratorUtil.generate();
        }

        // Member 생성
        Member member = Member.builder()
                .id(memberId)
                .email(email)
                .nickName(nickName)
                .profileImageUrl("https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png")
                .role(Role.USER)
                .build();

        return memberRepository.save(member);
    }
}