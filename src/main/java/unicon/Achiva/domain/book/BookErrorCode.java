package unicon.Achiva.domain.book;

import org.springframework.http.HttpStatus;
import unicon.Achiva.global.response.ErrorCode;

public enum BookErrorCode implements ErrorCode {
    BOOK_NOT_FOUND(3000, "해당 책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_BOOK_ACCESS(3001, "해당 책에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    INVALID_MAIN_ARTICLE(3002, "책의 첫 아티클은 book title 형식 이어야 합니다.", HttpStatus.BAD_REQUEST);

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    BookErrorCode(Integer code, String message, HttpStatus httpStatus) {
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
