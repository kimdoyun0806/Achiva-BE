package unicon.Achiva.domain.auth;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.auth.dto.*;
import unicon.Achiva.domain.member.MemberService;
import unicon.Achiva.domain.member.dto.MemberResponse;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

//    @Operation(summary = "자체 회원가입. presigned URL 발급 및 업로드가 선행되어야 함. - JWT 필요 X")
//    @PostMapping("api/auth/register")
//    public ResponseEntity<ApiResponseForm<CreateMemberResponse>> signup(@RequestBody MemberRequest requestDto) {
//        CreateMemberResponse createMemberResponse = authService.signup(requestDto);
//        return ResponseEntity.ok(ApiResponseForm.created(createMemberResponse, "회원가입 성공"));
//    }

    @Operation(summary = "회원등록. presigned URL 발급 및 업로드가 선행되어야 함. - JWT 필요")
    @PostMapping("api/auth/register")
    public ResponseEntity<ApiResponseForm<CreateMemberResponse>> signup(@RequestBody MemberRequest requestDto) {
        CreateMemberResponse createMemberResponse = authService.signup(requestDto);
        return ResponseEntity.ok(ApiResponseForm.created(createMemberResponse, "회원가입 성공"));
    }

    @Operation(summary = "해당 JWT 토큰의 유저정보 등록여부. Google/Apple 소셜 로그인 사용자는 자동 회원가입 처리됨. - JWT 필요")
    @PostMapping("api/auth/isinit")
    public ResponseEntity<ApiResponseForm<Boolean>> isInit() {
        UUID memberId = authService.getMemberIdFromToken();
        Boolean isInit = memberService.existsById(memberId);

        // Member가 없고 소셜 로그인 사용자이면 자동 회원가입
        if (!isInit) {
            try {
                authService.autoSignupSocialUser();
                isInit = true;
            } catch (Exception e) {
                // 에러 로깅
                System.err.println("========================================");
                System.err.println("autoSignupSocialUser 실패");
                System.err.println("에러 타입: " + e.getClass().getName());
                System.err.println("에러 메시지: " + e.getMessage());
                System.err.println("========================================");
                e.printStackTrace();
                // 소셜 로그인이 아니거나 자동 회원가입 실패 시 false 반환
                isInit = false;
            }
        }

        return ResponseEntity.ok(ApiResponseForm.success(isInit, "회원 등록 확인 결과:" + isInit.toString()));
    }

    @Operation(summary = "회원 정보 수정")
    @PutMapping("api/auth")
    public ResponseEntity<ApiResponseForm<MemberResponse>> updateMemberInfo(
            @RequestBody UpdateMemberRequest requestDto) {
        UUID memberId = authService.getMemberIdFromToken();
        MemberResponse memberResponse = authService.updateMember(memberId, requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "회원 정보 수정 성공"));
    }

    @Operation(summary = "회원탈퇴(유저 정보 삭제)")
    @DeleteMapping("api/auth")
    public ResponseEntity<ApiResponseForm<Void>> deleteMember() {
        UUID memberId = authService.getMemberIdFromToken();
        authService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "회원 탈퇴 성공"));
    }

    @Operation(summary = "이메일 중복 체크 - JWT 필요 X")
    @GetMapping("api/auth/check-email")
    public ResponseEntity<ApiResponseForm<CheckEmailResponse>> checkEmailDuplication(@RequestParam String email) {
        CheckEmailResponse checkEmailResponse = authService.validateDuplicateEmail(email);
        return ResponseEntity.ok(ApiResponseForm.success(checkEmailResponse, "이메일 중복 확인 성공"));
    }

    @Operation(summary = "닉네임 중복 체크 - JWT 필요 X")
    @GetMapping("api/auth/check-nickname")
    public ResponseEntity<ApiResponseForm<CheckNicknameResponse>> checkNicknameDuplication(@RequestParam String nickname) {
        CheckNicknameResponse checkNicknameResponse = authService.validateDuplicateNickName(nickname);
        return ResponseEntity.ok(ApiResponseForm.success(checkNicknameResponse, "닉네임 중복 확인 성공"));
    }

//    @Operation(summary = "이메일로 인증코드 전송 - JWT 필요 X")
//    @PostMapping("api/auth/send-verification-code")
//    public ResponseEntity<ApiResponseForm<SendVerificationCodeResponse>> sendCode(@RequestParam String email) {
//        SendVerificationCodeResponse sendVerificationCodeResponse = authService.sendVerificationCode(email);
//        return ResponseEntity.ok(ApiResponseForm.success(sendVerificationCodeResponse ,"인증 코드 전송 성공"));
//    }
//
//    @Operation(summary = "받은 이메일 인증코드로 인증 - JWT 필요 X")
//    @PostMapping("api/auth/verify-code")
//    public ResponseEntity<ApiResponseForm<VerifyCodeResponse>> verifyCode(@RequestParam String email,
//                                             @RequestParam String code) {
//        VerifyCodeResponse verifyCodeResponse = authService.verifyCode(email, code);
//        return ResponseEntity.ok(ApiResponseForm.success(verifyCodeResponse, "인증 코드 확인 성공"));
//    }
//
//    @Operation(summary = "이메일과 비밀번호 입력받아서 비밀번호 맞는지 확인")
//    @PostMapping("api/auth/verify-password")
//    public ResponseEntity<ApiResponseForm<CheckPasswordResponse>> verifyPassword(
//            @RequestBody CheckPasswordRequest request
//    ) {
//        CheckPasswordResponse verifyPasswordResponse = authService.checkPassword(request);
//        return ResponseEntity.ok(ApiResponseForm.success(verifyPasswordResponse, "비밀번호 확인 성공"));
//    }
//
//    @Operation(summary = "비밀번호 초기화")
//    @PostMapping("api/auth/reset-password")
//    public ResponseEntity<ApiResponseForm<ResetPasswordResponse>> resetPassword(@RequestBody ResetPasswordRequest request) {
//        ResetPasswordResponse resetPasswordResponse = authService.resetPassword(request);
//        return ResponseEntity.ok(ApiResponseForm.success(resetPasswordResponse, "비밀번호 재설정 성공"));
//    }
//
//    @Operation(summary = "로그인 - JWT 필요 X")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "일반 로그인 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\n  \"status\": \"success\", \"code\": 200, \"message\": \"로그인 성공\", \"data\": {\"id\": 1, \"email\": \"user@example.com\", \"nickname\": \"user\", \"birth\": \"2000-01-01\", \"gender\": \"MALE\", \"categories\": [\"공부\", \"운동\"], \"createdAt\": \"2023-01-01T00:00:00.000000\"}}"))),
//    })
//    @PostMapping("/api/auth/login")
//    public String login(
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "로그인 요청 JSON 데이터",
//                    required = true,
//                    content = @Content(
//                            schema = @Schema(type = "object", example = "{\"email\": \"user@example.com\", \"password\": \"password1234\"}")
//                    )
//            )
//            @RequestBody Map<String, String> loginRequest) {
//        return "로그인 성공"; // 실제 로그인 처리는 Security 필터에서 수행
//    }
}
