package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.exception.PointErrorCode;

import static io.hhplus.tdd.point.common.PointConstants.*;
import static io.hhplus.tdd.point.exception.PointErrorCode.*;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }


    //포인트 사용시 검증
    public PointErrorCode useValid(long amount) {
        if(!(point >= amount)) { //잔액 체크
            //throw new PointException(ERR_INSUFFICIENT_BALANCE);
            return ERR_INSUFFICIENT_BALANCE;
        }

        return null;
    }

    // 포인트 충전 검증
    public PointErrorCode chargeValid(long amount) {

        if (amount % CHARGE_UNIT != 0) {//충전단위
            return ERR_INVALID_CHARGE_UNIT;
        }

        if (this.point + amount > MAX_BALANCE) { //잔액 초과 최대 50만원
            return ERR_EXCEED_BALANCE;
        }
        return null;

    }

}
