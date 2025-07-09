package io.hhplus.tdd.point.common;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PointValidation {

    //히스토리 기준날짜별 횟수(사용, 충전)
    public long validUse(List<PointHistory> phList , TransactionType type , long time ){
        long cnt = 0;

        //이력 있을경우만 검사하기
        if(!phList.isEmpty()) {
            //하루 2회 사용
            cnt = phList.stream()
                    .filter(ph -> ph.type() == type) //사용타입
                    .filter(ph -> ph.updateMillis() >= time) //오늘 쓴거
                    .count();
        }
        return cnt;
    }
}
