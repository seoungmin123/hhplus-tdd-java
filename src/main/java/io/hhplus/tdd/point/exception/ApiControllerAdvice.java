package io.hhplus.tdd.point.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    /** 포인트 도메인 오류 */
    @ExceptionHandler(PointException.class)
    public ResponseEntity<ErrorResponse> handlePoint(PointException ex) {
        PointErrorCode ec = ex.getErrorCode();
        return ResponseEntity
                .status(ec.status)
                .body(new ErrorResponse(ec.code, ec.message));
    }

    /** 검증 오류 등 단순 잘못된 입력 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    /** 잡히지 않은 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        PointErrorCode ec = PointErrorCode.ERR_INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(ec.status)
                .body(new ErrorResponse(ec.code, ec.message));
    }
}
