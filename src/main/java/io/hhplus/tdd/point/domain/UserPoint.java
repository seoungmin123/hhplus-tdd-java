package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.exception.PointException;

import static io.hhplus.tdd.point.common.PointConstants.MAX_BALANCE;
import static io.hhplus.tdd.point.exception.PointErrorCode.ERR_EXCEED_BALANCE;
import static io.hhplus.tdd.point.exception.PointErrorCode.ERR_INSUFFICIENT_BALANCE;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    //잔액 부족 확인
    public void okUse(long amount) {
        boolean useAt = this.point >= amount;
        if(!useAt) {
            throw new PointException(ERR_INSUFFICIENT_BALANCE);
        }
    }

    //최대 잔고 확인
    public void maxCharge(long amount) {
        boolean chargeAt = this.point + amount < MAX_BALANCE;
        if(!chargeAt){
            throw new PointException(ERR_EXCEED_BALANCE);
        }
    }

    // 포인트 사용시 객체 자체를 새로 생성
    public UserPoint useAndUpdate(long amount) {
        return new UserPoint(id, this.point - amount, System.currentTimeMillis());
    }

    // 포인트 충전 후 새 객체 생성 메서드 추가
    public UserPoint chargeAndUpdate(long amount) {
        return new UserPoint(id, this.point + amount, System.currentTimeMillis());
    }

}
