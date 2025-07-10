package io.hhplus.tdd;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.PointValidation;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.hhplus.tdd.point.common.PointConstants.getTodayStartMillis;
import static io.hhplus.tdd.point.domain.TransactionType.CHARGE;
import static io.hhplus.tdd.point.domain.TransactionType.USE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


/* g할꺼 TODO
실제 객체생성
- 히스토리 정렬(의미가있나..)
- 충전, 사용시 히스토리가 내역 잘 저장되는지 -
*/
public class PointServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private final PointValidation pointValidation = new PointValidation();

    private PointService service;

    private final long USER_ID     = 5L;

    PointHistory older ;
    PointHistory newer ;

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

    @DisplayName("히스토리 정렬순서 일치")
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

    @DisplayName("충전 시 히스토리 내역 저장")
    @Test
    void 충전_히스토리_저장_검증() {
        // given
        long chargeAmount = 5_000L;
        long id = 1L;

        // 초기 사용자 등록
        userPointTable.selectById(id);

        // when
        service.useCharge(id, chargeAmount);

        // then
        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
        assertThat(histories).hasSize(1); //히스토리한개

        PointHistory history = histories.get(0);
        assertThat(history.userId()).isEqualTo(id);
        assertThat(history.amount()).isEqualTo(chargeAmount);
        assertThat(history.type()).isEqualTo(TransactionType.CHARGE);
    }

    @DisplayName("포인트 사용 시 히스토리 내역이 저장")
    @Test
    void 사용_히스토리_저장_검증() {
        // given
        long id = 1L;
        userPointTable.insertOrUpdate(id, 10_000L);
        service.usePoint(id, 3_000L);

        // then
        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.USE);
    }


    @Test
    @DisplayName("포인트 충전 시, 잔액과 히스토리가 정상적으로 반영")
    void 충전_성공_통합테스트() {
        // given
        long id = 1L;
        long chargeAmount = 10_000L;

        userPointTable.selectById(id);

        // when
        UserPoint result = service.useCharge(id, chargeAmount);

        // then
        AssertionsForClassTypes.assertThat(result.point()).isEqualTo(10_000L);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("포인트 사용 시, 잔액 차감과 히스토리가 정상 반영")
    void 사용_성공_통합테스트() {
        // given
        long id = 1L;
        userPointTable.selectById(id);
        userPointTable.insertOrUpdate(id, 20_000L);

        // when
        UserPoint result = service.usePoint(id, 5_000L);

        // then
        AssertionsForClassTypes.assertThat(result.point()).isEqualTo(15_000L);
        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.USE);
    }



}
