package io.hhplus.tdd;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static io.hhplus.tdd.point.common.PointMessages.*;
import static io.hhplus.tdd.point.common.PointMessages.DAILY_USE_LIMIT_EXCEEDED;
import static io.hhplus.tdd.point.common.PointMessages.EXCEED_MAX_USE;
import static io.hhplus.tdd.point.common.PointMessages.INVALID_CHARGE_UNIT;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


public class PointServiceTest {

    private PointService service;
    /**예외마다 테스트를 진행하는게 맞는지 모르겠습니다.
     * 에러 메시지 자체도 너무 강경합 테스트라서 옳은 테스트일까요 */

    @BeforeEach
    void init() {
        UserPointTable userPointTable = new UserPointTable();
        PointHistoryTable historyTable = new PointHistoryTable();
        PointVaildation validation = new PointVaildation();
        service           = new PointService(userPointTable, historyTable, validation);
        // 공통 세팅: id=1 에 70_000원 충전
        service.useCharge(1L, 70000);
    }

    @Test
    void 포인트_조회(){
        UserPoint up = service.getPoint(1L);
        assertThat(up.point()).isEqualTo(70000);
    }

    //히스토리를 조회하러면 넣어야하는데 그러면 독립적이 아니게되고
    //그냥 가상데이터를 넣자니 좀 이상한것같아서 어떤게 더 맞는방법인지 질문드립니다.
    @Test
    void 히스토리_조회(){
        service.usePoint(1L,500);
        service.usePoint(1L,1000);
        service.useCharge(1L,5000);

        List<PointHistory> phList = service.getHistory(1L);

        assertThat(phList).extracting(ph-> ph.userId(),ph-> ph.amount(), ph ->ph.type())
                .contains(tuple( 1L, 500L, TransactionType.USE)
                ,tuple( 1L, 70000L, TransactionType.CHARGE)
                        ,tuple( 1L, 5000L, TransactionType.CHARGE));
    }

    @ParameterizedTest(name = "{0}원을 사용한다")
    @CsvSource({
            "5000,65000",
            "10000, 60000"})
    void 포인트_사용_성공(long 쓴돈, long 기대) {
        UserPoint up = service.usePoint(1L, 쓴돈);
        assertThat(up.point()).isEqualTo(기대);
    }

    @ParameterizedTest(name = "{0}원을 충전하면 금액이 {1}원이 남음")
    @CsvSource({
            "5000,75000",
            "10000, 80000"})
    void 포인트_충전_성공(long 충전 , long 기대) {
        UserPoint up = service.useCharge(1L, 충전);
        assertThat(up.point()).isEqualTo(기대);
    }

    @Test
    void 잔액부족_예외() {
        assertThatThrownBy(() -> service.usePoint(1L, 70001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INSUFFICIENT_BALANCE);
    }

    @Test
    void 최대_잔고_50만원_예외() {
        assertThatThrownBy(() -> service.useCharge(1L, 40_000_000) )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXCEED_BALANCE);
    }

    @Test
    void 충전금액이_0원이하면_예외발생() {
        assertThatThrownBy(() -> service.useCharge(1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BELOW_MINIMUM_CHARGE);
    }

    @ParameterizedTest(name = "{0}원 충전시 5000원단위로 충전가능하다.")
    @ValueSource(longs = {1000,2000,3000})
    void 충전금액이_5000원단위가_아니면_예외발생(long amount) {
        assertThatThrownBy(() -> service.useCharge(1L, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CHARGE_UNIT);
    }

    @Test
    void 하루에_2번이상_사용시_예외_발생() {
        long id = 1L;

        // 1차 사용
        service.usePoint(id, 300);
        // 2차 사용
        service.usePoint(id, 500);

        // 3차 사용 (횟수제한 )
        assertThatThrownBy(() -> service.usePoint(id, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DAILY_USE_LIMIT_EXCEEDED);
    }

    @ParameterizedTest(name = "{0}은 최대사용금액 을 넘었습니다.")
    @ValueSource(longs = {1000000,500000})
    void 최대사용금액_100000원_넘으면_예외발생(long amount) {
        assertThatThrownBy(() -> service.usePoint(1L, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXCEED_MAX_USE);
    }
}
