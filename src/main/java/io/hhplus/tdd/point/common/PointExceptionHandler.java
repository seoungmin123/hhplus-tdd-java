package io.hhplus.tdd.point.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PointExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>>handleBadRequest(IllegalArgumentException ex) {

        return ResponseEntity.ok(ApiResponse.fail(ex.getMessage()));

    }

}
