package io.hhplus.tdd.point.common;

import java.time.LocalDate;
import java.time.ZoneId;

public class PointConstants {
    public static final long MAX_USE_POINT = 100_000; //하루 최대 사용금액
    public static final long MAX_BALANCE = 500_000; //최대 보유 가능 잔고
    public static final long CHARGE_UNIT = 5000;  //충전 단위
    public static final int MAX_DAILY_USE_COUNT = 2; //하루 사용 가능 횟수

    //오늘시간 가져오기
    public static long getTodayStartMillis() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private PointConstants() {} // 인스턴스화 방지
}
