package io.hhplus.tdd;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.PointValidation;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static io.hhplus.tdd.point.common.PointConstants.getTodayStartMillis;
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
/* g할꺼 TODO
- 히스토리 정렬(의미가있나..)
- 사용시 예외확인
- 충전, 사용시 히스토리가 내역 잘 저장되는지
*/
public class PointServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService service;
    private final PointValidation pointValidation = new PointValidation();

    private final long USER_ID     = 5L;

    PointHistory older ;
    PointHistory newer ;
    PointHistory history_test ;

    @BeforeEach
    void init() {
        // 의존 객체 직접 생성
        userPointTable     = new UserPointTable();
        pointHistoryTable  = new PointHistoryTable();

        // 서비스 인스턴스
        service = new PointService(userPointTable, pointHistoryTable,pointValidation);

        // 초기 데이터 세팅
        long INIT_POINT = 70_000L;
        userPointTable.insertOrUpdate(USER_ID, INIT_POINT);
        //사용 두번해야댐
        older = new PointHistory(1, USER_ID, INIT_POINT,CHARGE , System.currentTimeMillis());
        newer = new PointHistory(2, USER_ID,100, USE,System.currentTimeMillis());


    }


    @Test
    void 히스토리_정렬조회_확인() {

        given(pointHistoryTable.selectAllByUserId(USER_ID)).willReturn(List.of(older, newer));

        List<PointHistory> sort = service.getHistory(USER_ID);

        assertThat(sort).containsExactly(newer, older);
        then(pointHistoryTable).should().selectAllByUserId(USER_ID);
    }

    @DisplayName("오늘 USE가 두 번이면 cnt=2")
    @Test
    void validUse_오늘두번사용() {
        // given: 오늘 날짜만큼 millis 계산
        long todayStart = getTodayStartMillis();

        List<PointHistory> list = List.of(
                new PointHistory(1, 1L, 100, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(2, 1L, 200, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(2, 1L, 200, CHARGE, System.currentTimeMillis())
        );

        // when
        long cnt = new PointValidation().validUse(list, TransactionType.USE, todayStart);

        // then
        assertThat(cnt).isEqualTo(2);
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

    @ParameterizedTest(name = "{0}원 충전시 5000원단위로 충전가능하다.")
    @ValueSource(longs = {1000,2000,3000})
    void 충전금액이_5000원단위가_아니면_예외발생(long amount) {
        assertThatThrownBy(() -> service.useCharge(1L, amount))
                .isInstanceOf(PointException.class)
                .hasMessage(ERR_INVALID_CHARGE_UNIT.message);
    }



    @ParameterizedTest(name = "{0}은 최대사용금액 을 넘었습니다.")
    @ValueSource(longs = {1000000,500000})
    void 최대사용금액_100000원_넘으면_예외발생(long amount) {
        assertThatThrownBy(() -> service.usePoint(1L, amount))
                .isInstanceOf(PointException.class)
                .hasMessage(ERR_EXCEED_MAX_USE.message);
    }
}
