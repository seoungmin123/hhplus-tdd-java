package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.common.PointConstants.MAX_BALANCE;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    //잔액 부족 확인
    public boolean okUse(long amount) {
        return this.point >= amount; //T
    }

    //최대 잔고 확인
    public boolean maxCharge(long amount) {
        return this.point + amount < MAX_BALANCE; //T
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
