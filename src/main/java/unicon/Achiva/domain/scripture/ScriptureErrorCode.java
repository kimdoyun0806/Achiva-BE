package unicon.Achiva.domain.scripture;

import org.springframework.http.HttpStatus;
import unicon.Achiva.global.response.ErrorCode;

public enum ScriptureErrorCode implements ErrorCode {
    INVALID_SCRIPTURE_ID(7000, "유효하지 않은 성경 권입니다.", HttpStatus.BAD_REQUEST),
    INVALID_START_CHAPTER(7001, "시작 장은 1장 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_END_CHAPTER(7002, "종료 장은 시작 장 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    CHAPTER_OUT_OF_RANGE(7003, "장 수가 해당 성경 권 범위를 벗어났습니다.", HttpStatus.BAD_REQUEST),
    INVALID_COMPLETED_CHAPTERS(7004, "누적 진도 장 수가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    COMPLETED_CHAPTERS_BEFORE_END(7005, "누적 진도는 이번 읽기 종료 장보다 작을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_YEAR_MONTH(7006, "yearMonth 형식이 올바르지 않습니다. YYYY-MM 형식이어야 합니다.", HttpStatus.BAD_REQUEST);

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    ScriptureErrorCode(Integer code, String message, HttpStatus httpStatus) {
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
