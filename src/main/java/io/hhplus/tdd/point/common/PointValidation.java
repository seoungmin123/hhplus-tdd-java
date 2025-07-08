package io.hhplus.tdd.point.common;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.exception.PointException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static io.hhplus.tdd.point.common.PointConstants.*;
import static io.hhplus.tdd.point.exception.PointErrorCode.*;

public class PointValidation {

    //오늘시간
    public static long getTodayStartMillis() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }


    //사용 유효성 -하루 2회사용 , 한번 최대사용금액 100000
    public void validUse(List<PointHistory> phList, long amount){


        //최대 사용금액
        if (MAX_USE_POINT < amount){
            throw new PointException(ERR_EXCEED_MAX_USE);//최대사용금액은 100000원
        }

        //이력없으면 검사안함
        if(phList.isEmpty()){
            return;
        }

        //하루 2회 사용
        long cnt = phList.stream()
                .filter(ph -> ph.type()== TransactionType.USE ) //사용타입
                .filter(ph -> ph.updateMillis() >= getTodayStartMillis()) //오늘 쓴거
                .count();

        if(cnt >= MAX_DAILY_USE_COUNT ){ // 0회 사용전 , 첫 사용후, 두번째부터 안되야함
            throw new PointException(ERR_DAILY_USE_LIMIT_EXCEEDED);//하루 2번사용
        }

    }
}
