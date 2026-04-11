package unicon.Achiva.domain.member;

import org.springframework.http.HttpStatus;
import unicon.Achiva.global.response.ErrorCode;

public enum MemberErrorCode implements ErrorCode {
    INVALID_TOKEN(1000, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    DUPLICATE_EMAIL(1001, "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME(1002, "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    VERIFICATION_NOT_FOUND(1003, "해당 이메일에 대한 인증 요청이 존재하지 않습니다", HttpStatus.BAD_REQUEST),
    VERIFICATION_EXPIRED(1004, "인증 요청이 만료되었습니다.", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_MISMATCH(1005, "인증 코드가 틀렸습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH(1006, "비밀번호와 비밀번호 확인이 일치하지 않습니다", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND(1007, "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1008, "비밀번호가 틀렸습니다.", HttpStatus.UNAUTHORIZED),
    MEMBER_ALREADY_EXISTS(1009, "이미 회원가입이 완료된 사용자입니다.", HttpStatus.CONFLICT),
    COGNITO_DELETE_FAILED(1010, "Cognito 계정 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_NICKNAME(1011, "닉네임 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    ;

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    MemberErrorCode(Integer code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
