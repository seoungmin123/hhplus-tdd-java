package io.hhplus.tdd.point.exception;

import org.springframework.http.HttpStatus;

import static io.hhplus.tdd.point.exception.PointErrorMsg.MSG_BELOW_MINIMUM_CHARGE;

public enum PointErrorCode {
    ERR_BELOW_MINIMUM_CHARGE("E001", MSG_BELOW_MINIMUM_CHARGE,      HttpStatus.BAD_REQUEST),
    ERR_INVALID_CHARGE_UNIT("E002", "충전 금액은 5000원 단위", HttpStatus.BAD_REQUEST),
    ERR_EXCEED_MAX_USE("E003", "최대사용금액은 100000원",  HttpStatus.BAD_REQUEST),
    ERR_DAILY_USE_LIMIT_EXCEEDED("E004", "하루 2번사용",        HttpStatus.BAD_REQUEST),
    ERR_EXCEED_BALANCE("E005", "최대 잔고 보유는 50만원까지만 가능", HttpStatus.BAD_REQUEST), //MAX_BALANCE 50만원
    ERR_INSUFFICIENT_BALANCE("E006", "포인트 부족",             HttpStatus.BAD_REQUEST),

    // 범용
    ERR_INTERNAL_SERVER_ERROR("E500", "서버 내부 오류",           HttpStatus.INTERNAL_SERVER_ERROR);

    public final String code;
    public final String message;
    public final HttpStatus status;

    PointErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
