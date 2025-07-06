package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    //잔액부족 확인
    public boolean okUse(long amount) {
        return this.point >= amount; //T
    }

    //최대잔고확인
    public boolean maxCharge(long amount) {
        return this.point + amount < 500000; //T
    }

    //사용하고 남은 액수
    public long use(long amount){
        return this.point - amount;
    }


}
