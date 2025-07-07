package io.hhplus.tdd.point;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.hhplus.tdd.point.common.PointConstants.*;
import static io.hhplus.tdd.point.common.PointMessages.*;

public class PointVaildation {

    //오늘시간
    public static long getTodayStartMillis() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    //충전 유효성 - 항상양수 , 충전단위 5000
    public void validCharge(long amount){
        //항상 양수로 충전
        if (amount <= 0) {
            throw new IllegalArgumentException(BELOW_MINIMUM_CHARGE);//0원 이하 충전 불가
        }

        //충전단위 5000원
        if (amount % CHARGE_UNIT != 0) {
            throw new IllegalArgumentException(INVALID_CHARGE_UNIT);//충전 금액은 5000원 단위
        }


    }


    //사용 유효성 -하루 2회사용 , 한번 최대사용금액 100000
    public void validUse(List<PointHistory> phList, long amount){
        //최대 사용금액
        if (MAX_USE_POINT < amount){
            throw new IllegalArgumentException(EXCEED_MAX_USE);//최대사용금액은 100000원
        }

        //하루 2회 사용
        long cnt = phList.stream()
                .filter(ph -> ph.type()==TransactionType.USE ) //사용타입
                .filter(ph -> ph.updateMillis() >= getTodayStartMillis()) //오늘 쓴거
                .count();

        if(cnt >= MAX_DAILY_USE_COUNT ){ // 0회 사용전 , 첫 사용후, 두번째부터 안되야함
            throw new IllegalArgumentException(DAILY_USE_LIMIT_EXCEEDED);//하루 2번사용
        }

    }

}
