package unicon.Achiva.domain.goal;

import org.springframework.http.HttpStatus;
import unicon.Achiva.global.response.ErrorCode;

public enum GoalErrorCode implements ErrorCode {
    GOAL_NOT_FOUND(3000, "해당 목표를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(3001, "해당 목표에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_TEXT_LENGTH(3002, "목표 내용은 1자 이상 200자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY(3003, "유효하지 않은 목표 카테고리입니다.", HttpStatus.BAD_REQUEST),
    ;

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    GoalErrorCode(Integer code, String message, HttpStatus httpStatus) {
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
