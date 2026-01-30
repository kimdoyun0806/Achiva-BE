package unicon.Achiva.domain.push;

import org.springframework.http.HttpStatus;
import unicon.Achiva.global.response.ErrorCode;

public enum PushErrorCode implements ErrorCode {
    INVALID_LINK_TOKEN(4000, "유효하지 않은 링크 토큰입니다.", HttpStatus.BAD_REQUEST),
    EXPIRED_LINK_TOKEN(4001, "만료된 링크 토큰입니다.", HttpStatus.BAD_REQUEST),
    ALREADY_USED_LINK_TOKEN(4002, "이미 사용된 링크 토큰입니다.", HttpStatus.BAD_REQUEST),
    INVALID_EXPO_TOKEN(4003, "유효하지 않은 Expo 푸시 토큰입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_PUSH_TOKEN(4004, "이미 등록된 푸시 토큰입니다.", HttpStatus.CONFLICT),
    PUSH_DISABLED(4005, "푸시 알림이 비활성화된 사용자입니다.", HttpStatus.FORBIDDEN),
    PUSH_SEND_FAILED(4006, "푸시 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_NOT_FOUND_FOR_PUSH(4007, "푸시 대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ;

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    PushErrorCode(Integer code, String message, HttpStatus httpStatus) {
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
