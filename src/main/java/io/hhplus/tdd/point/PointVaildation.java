package io.hhplus.tdd.point;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointVaildation {

    //오늘시간
   public static long todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();

    //충전 유효성 - 항상양수 , 최소충전 1000, 충전단위 5000
    public Map<String,Object> validCharge(long amount){
        Map<String,Object> resMap = new HashMap();
        Boolean status = Boolean.TRUE;
        String msg ="";

        //항상 양수로 충전
        if (amount <= 0) {
            msg = "0원 이하 충전 불가";
            status = Boolean.FALSE;
        }
        //최소 충전금액(1000원)
        if (amount <= 1000) {
            msg = "최소 충전 금액은 1000원을 초과";
            status = Boolean.FALSE;
        }

        //충전단위 5000원
        if (amount % 5000 != 0) {
            msg = "충전 금액은 5000원 단위";
            status = Boolean.FALSE;
        }

        resMap.put("msg" , msg);
        resMap.put("status", status);
        return resMap;
    }


    //사용 유효성 -하루 2회사용 , 한번 최대사용금액 50000
    public Map<String,Object> validUse(List<PointHistory> phList, long amount){
        Map<String,Object> resMap = new HashMap();
        Boolean status = Boolean.TRUE;
        String msg ="";

        //최대 사용금액 (50000)
        if (50000 < amount){
            msg = "최대사용금액은 50000원";
            status = Boolean.FALSE;
        }

        //하루 2회 사용
        long cnt = phList.stream()
                .filter(ph -> ph.type()==TransactionType.USE ) //사용타입
                .filter(ph -> ph.updateMillis() >= todayStart ) //오늘 쓴거
                .count();

        if(cnt > 2){
            msg = "하루 2번사용";
            status = Boolean.FALSE;
        }

        resMap.put("msg" , msg);
        resMap.put("status", status);
        return resMap;
    }

}
