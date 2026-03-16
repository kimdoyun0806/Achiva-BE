package unicon.Achiva.domain.moim;

import org.springframework.http.HttpStatus;
import unicon.Achiva.global.response.ErrorCode;

public enum MoimErrorCode implements ErrorCode {
    MOIM_NOT_FOUND(1300, "모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1301, "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    MOIM_ALREADY_FULL(1302, "모임의 정원이 꽉 찼습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_JOINED(1303, "이미 가입한 모임입니다.", HttpStatus.CONFLICT),
    UNAUTHORIZED_ACTION(1304, "모임에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN)
    ;

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    MoimErrorCode(Integer code, String message, HttpStatus httpStatus) {
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
