package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.exception.PointException;

import static io.hhplus.tdd.point.common.PointConstants.CHARGE_UNIT;
import static io.hhplus.tdd.point.common.PointConstants.MAX_BALANCE;
import static io.hhplus.tdd.point.exception.PointErrorCode.*;

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

    // 포인트 사용시 객체 자체를 새로 생성
    //TODO 검증과 리턴값을 쪼갤까 고민을 해봣는데 검증자체만 테스트하는고 서비스쪽에서 검증을 뺴먹는 일자체도 없을것같고
    // 테스트 자체도 간단해진다고 생각했씁니다. 또한 검증 자체만하는 테스트 자체로서도 의미가없어서 합쳤는데 이런식으로 하는것에대해 어떻게 생각하시는지 궁금합니다.
    //추가로 자체 메서드를 빼는게 나은가 아니면 내부에서 관리를 하는게 나은지 궁금합니다..
    public UserPoint useAndUpdate(long amount) {

        okUse(amount);

        return new UserPoint(id, this.point - amount, System.currentTimeMillis());
    }

    // 포인트 충전 후 새 객체 생성 메서드 추가
    public UserPoint chargeAndUpdate(long amount) {
        // 검증
        if (amount <= 0) {
            throw new PointException(ERR_BELOW_MINIMUM_CHARGE); //잔액 0원
        }
        if (amount % CHARGE_UNIT != 0) {
            throw new PointException(ERR_INVALID_CHARGE_UNIT); //충전단위
        }

        if (this.point + amount > MAX_BALANCE) { //잔액 초과
            throw new PointException(ERR_EXCEED_BALANCE);
        }

        return new UserPoint(id, this.point + amount, System.currentTimeMillis());
    }

}
