package unicon.Achiva.global.response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleGeneralException(GeneralException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("[Error Log] requestUrl: {}, requestMethod: {}, userId: {}, clientIp: {}, exception: {}, message: {}, responseStatus: {}",
                request.getRequestURI(), request.getMethod(), (request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : "Anonymous", request.getRemoteAddr(), "HttpMessageNotReadableException", "요청 형식이 올바르지 않습니다.", 400);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseForm.error(400, "요청 형식이 올바르지 않습니다."));
    }


    /**
     * @Valid (DTO 유효성 검사) 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                            HttpServletRequest request) {
        BindingResult bindingResult = ex.getBindingResult();

        // 여러 필드 에러를 모두 합쳐서 표시
        String errorMessage = bindingResult.getFieldErrors().stream()
                .map(fieldError -> String.format("[%s] %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        if (errorMessage.isEmpty()) errorMessage = "입력 값 유효성 검사 실패";

        log.warn("[Validation Warn] url={}, method={}, ip={}, msg={}",
                request.getRequestURI(), request.getMethod(), request.getRemoteAddr(), errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseForm.error(400, errorMessage));
    }

    /**
     * Hibernate Entity Validation 실패 (ConstraintViolationException 감싸진 경우)
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleTransactionSystemException(TransactionSystemException ex,
                                                                                  HttpServletRequest request) {

        Throwable cause = ex.getRootCause();
        if (cause instanceof ConstraintViolationException violationException) {
            // interpolatedMessage (실제 메시지)
            String errorMessage = violationException.getConstraintViolations().stream()
                    .map(v -> String.format("[%s] %s", v.getPropertyPath(), v.getMessage()))
                    .collect(Collectors.joining(", "));

            log.warn("[Entity Validation Warn] url={}, method={}, ip={}, msg={}",
                    request.getRequestURI(), request.getMethod(), request.getRemoteAddr(), errorMessage);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseForm.error(400, errorMessage));
        }

        log.error("[Transaction Error] url={}, method={}, msg={}",
                request.getRequestURI(), request.getMethod(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseForm.error(500, "서버 내부 트랜잭션 오류"));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleInvalidSortProperty(
            PropertyReferenceException ex,
            HttpServletRequest request
    ) {
        log.warn("[Validation] 잘못된 정렬 필드 요청: {}, from URL: {}",
                ex.getPropertyName(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseForm.error(
                        400,
                        "정렬 기준 '" + ex.getPropertyName() + "'은(는) 존재하지 않습니다."
                ));
    }
}
