package unicon.Achiva.domain.organization;

import org.springframework.http.HttpStatus;
import unicon.Achiva.global.response.ErrorCode;

public enum OrganizationErrorCode implements ErrorCode {
    ORGANIZATION_NOT_FOUND(1600, "상위 모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ORGANIZATION_INACTIVE(1601, "현재 사용할 수 없는 상위 모임입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORGANIZATION_PASSWORD(1602, "상위 모임 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ORGANIZATION_REQUIRED(1603, "회원가입 시 상위 모임 선택이 필요합니다.", HttpStatus.BAD_REQUEST);

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    OrganizationErrorCode(Integer code, String message, HttpStatus httpStatus) {
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
