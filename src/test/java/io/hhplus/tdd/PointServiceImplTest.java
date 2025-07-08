package io.hhplus.tdd;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.PointValidation;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.service.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static io.hhplus.tdd.point.domain.TransactionType.CHARGE;
import static io.hhplus.tdd.point.domain.TransactionType.USE;
import static io.hhplus.tdd.point.exception.PointErrorCode.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**TODO
 * 히스토리 정렬확인
 * 조회시 최초사용자는 포인트 0원
 * usePoint 통테
 * useCharge 통테
 * */
public class PointServiceImplTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointValidation pointValidation;
    private PointServiceImpl service;

    private final long USER_ID     = 5L;
    private final long INIT_POINT  = 70_000L;

    PointHistory older ;
    PointHistory newer ;

    @BeforeEach
    void init() {
        // 의존 객체 직접 생성
        userPointTable     = new UserPointTable();
        pointHistoryTable  = new PointHistoryTable();
        pointValidation    = new PointValidation();

        // 서비스 인스턴스
        service = new PointServiceImpl(userPointTable, pointHistoryTable, pointValidation);

        // 초기 데이터 세팅
        userPointTable.insertOrUpdate(USER_ID, INIT_POINT);
        older = new PointHistory(1, USER_ID, INIT_POINT ,CHARGE , System.currentTimeMillis());
        newer = new PointHistory(2, USER_ID,100, USE,System.currentTimeMillis());
    }


    @ParameterizedTest
    @ValueSource(longs = {1L,2L})
    void 최초사용자는_0원(long newId) {
        //given
        userPointTable.selectById(newId);

        // when
        UserPoint actual = service.getPoint(newId);

        // then
        assertThat(actual.point()).isZero();

    }


    @Test
    void 히스토리_정렬조회_확인() {

        given(pointHistoryTable.selectAllByUserId(USER_ID)).willReturn(List.of(older, newer));

        List<PointHistory> sort = service.getHistory(USER_ID);

        assertThat(sort).containsExactly(newer, older);
        then(pointHistoryTable).should().selectAllByUserId(USER_ID);
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
                .isInstanceOf(PointException.class)
                .hasMessage(ERR_INSUFFICIENT_BALANCE.message);
    }

    @Test
    void 최대_잔고_50만원_예외() {
        assertThatThrownBy(() -> service.useCharge(1L, 40_000_000) )
                .isInstanceOf(PointException.class)
                .hasMessage(ERR_EXCEED_BALANCE.message);
    }

    @Test
    void 충전금액이_0원이하면_예외발생() {
        assertThatThrownBy(() -> service.useCharge(1L, 0))
                .isInstanceOf(PointException.class)
                .hasMessage(ERR_BELOW_MINIMUM_CHARGE.message);
    }

    @ParameterizedTest(name = "{0}원 충전시 5000원단위로 충전가능하다.")
    @ValueSource(longs = {1000,2000,3000})
    void 충전금액이_5000원단위가_아니면_예외발생(long amount) {
        assertThatThrownBy(() -> service.useCharge(1L, amount))
                .isInstanceOf(PointException.class)
                .hasMessage(ERR_INVALID_CHARGE_UNIT.message);
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
                .isInstanceOf(PointException.class)
                .hasMessageContaining(ERR_DAILY_USE_LIMIT_EXCEEDED.message);
    }

    @ParameterizedTest(name = "{0}은 최대사용금액 을 넘었습니다.")
    @ValueSource(longs = {1000000,500000})
    void 최대사용금액_100000원_넘으면_예외발생(long amount) {
        assertThatThrownBy(() -> service.usePoint(1L, amount))
                .isInstanceOf(PointException.class)
                .hasMessage(ERR_EXCEED_MAX_USE.message);
    }
}
