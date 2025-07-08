package io.hhplus.tdd.point.exception;

public class PointException extends RuntimeException {
    private final PointErrorCode errorCode;

    public PointException(PointErrorCode errorCode) {
        super(errorCode.message);
        this.errorCode = errorCode;
    }

    public PointErrorCode getErrorCode() {
        return errorCode;
    }
}
