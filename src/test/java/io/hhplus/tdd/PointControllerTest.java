package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.util.Assert.isInstanceOf;

@ExtendWith(MockitoExtension.class)
public class PointControllerTest {


    UserPointTable userPointTable = new UserPointTable();
    PointHistoryTable pointHistoryTable = new PointHistoryTable();
    PointVaildation pointVaildation = new PointVaildation();
    PointService pointService = new PointService(userPointTable, pointHistoryTable,pointVaildation);
    PointController pointController = new PointController(pointService);

    @BeforeEach
    /**
     * 테스트 자체가 독립적이어야해서 매번 객체생성 초기화
     * */
    void init() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        PointVaildation pointVaildation= new PointVaildation();
        PointService pointService = new PointService(userPointTable, pointHistoryTable,pointVaildation);
        PointController pointController = new PointController(pointService);

    }



    @ParameterizedTest(name = "{0}의 포인트 값 자체를 조회한다.")
    @CsvSource({
            "1,5000",
            "2,10000"})
    void 포인트_조회(long id, long point) {
        //가정 비교 데이터 추가
        pointService.charge(id,point); //TODO 충전아닌가
       // userPointTable.insertOrUpdate(id,point);

        // 포인트 조회
        UserPoint userPoint = pointService.getPoint(id);

        // 포인트가 조회값 자체가 일치하는건지 확인
        assertThat(userPoint.point()).isEqualTo(point);
    }

    @ParameterizedTest(name = "{0}의 포인트 히스토리를 조회한다.")
    @CsvSource({
            "1,5000,CHARGE,1",
            "2, 8000,USE,2"})
    void 히스토리_조회(long id , long amount, TransactionType type, long millis) {
        //가정 비교 데이터 추가
        UserPoint userPoint = userPointTable.selectById(id);
        pointService.saveHistory(userPoint,amount, type);

        // 포인트 조회_list
        List<PointHistory> result = pointService.getHistory(id);

        // 출력 확인
        //result리스트 중에 내가 넣은거 한개라도 있는지 확인하기
        boolean match = result.stream().anyMatch(ph ->
                ph.amount() == amount &&
                ph.type() == type
             // &&  ph.updateMillis() == millis
        );

        assertThat(match).isTrue();
    }


    @ParameterizedTest(name = "{0}원을 충전하면 금액이 {0}원 증가한다")
    @ValueSource(longs = {5000,10000})
    void 포인트_충전(long point) {
        long id = 1L;
        // 특정 유저에게 충전해줌
        UserPoint resUser = pointService.charge(id, point);

        // 충전 넣은만큼 잘됐나 행위 검증
        assertThat(resUser.point()).isEqualTo(point);
    }


    //리스트의 경우 연속 테스트를 하는게 맞는지 아님 단일?
    @Test
    void 히스토리_저장(long id, long amount , TransactionType type) {
        //객체생성
        UserPoint userPoint = userPointTable.selectById(id);

        //히스토리 테이블에 저장 1번
        pointService.saveHistory(userPoint , 3000 , TransactionType.USE);
        //히스토리 테이블에 저장 2번
        pointService.saveHistory(userPoint , 5000 , TransactionType.CHARGE);

        //히스토리 출력
        List<PointHistory> phList = pointService.getHistory(id);

        // 출력 확인
        //phList 중에 내가 넣은거 있는지 확인하기
        assertThat(phList)
                .extracting(PointHistory::amount, PointHistory::type)
                .containsExactlyInAnyOrder(tuple(3000L, TransactionType.USE)
                        ,tuple(5000L, TransactionType.CHARGE)
        );

    }


    /**
     * 테스트 결과가 예외인경우에 처리를 해주었는데 예외는 그냥 예외가 터지게 둬도될까요?
     *
     * 왜냐면 이경우에는 테스트 코드가 너무 강결합이라 코드를 수정하면
     * 오류 메시지나 오류등등을 다 수정해야하니까 좋은 테스트는 아닌거같아서 질문드립니다.
     * 아니면 예외랑 정상케이스를 따로 테스트 해야 맞는걸까요 ??
     * */
    @ParameterizedTest(name = "{1}에서 {2}원을 사용하면 금액이 {3}원이 된다.")
    @CsvSource({
            "1,5000,200,4800",
            "2,10000,1000,9000"
    })
    void 포인트_사용_성공(long id, long 초기금액, long 사용금액, long 기대잔액) {
        //가정 비교 데이터 추가
        //userPointTable.insertOrUpdate(id, 초기금액);
        pointService.charge(id,초기금액);

        // 사용함
        UserPoint resUser= pointService.use(id, 사용금액);

        // 사용 잘됐나 행위 검증
        assertThat(resUser.point()).isEqualTo(기대잔액);

        /*if(기대잔액 == -1) {
            assertThatThrownBy(() -> pointService.use(id, 사용금액))
                    .isInstanceOf(IllegalAccessError.class)
                    .hasMessage("포인트가 부족합니다.");
        }else {
            assertThat(resUser.point()).isEqualTo(기대잔액);
        }*/
    }

    //테스트만을 위한 초기값 세팅 메서드를 만드는게 맞는지
    @ParameterizedTest(name = "{1}에서 {2}원을 사용하면 금액이 {3}원이 된다.")
    @CsvSource({
            "1,5000,200",
            "2,3000,10000000"
    })
    void 포인트_사용_실패(long id, long 초기금액, long 사용금액) {
        //가정 비교 데이터 추가
        //userPointTable.insertOrUpdate(id, 초기금액);
       // pointService.charge(id,초기금액);
        pointService.testPoint(id,초기금액);

        // 사용함
     //  UserPoint resUser= pointService.use(id, 사용금액);

        // 사용 잘됐나 행위 검증
        assertThatThrownBy(() -> pointService.use(id, 사용금액))
                .isInstanceOf(IllegalAccessError.class);
                //.hasMessage("포인트가 부족합니다.");

    }



    @Test
    void 최대_잔고_50만원() {
        long id = 1L;

        // 특정 유저에게 충전해줌
        pointController.charge(id, 50000);
        pointController.charge(id, 100000);

        assertThatThrownBy(() -> pointController.charge(id, 4000000) )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 잔고 보유는 50만원까지만 가능합니다.");
    }


    @Test
    void 충전금액이_0원이하면_예외발생() {
        long id = 1L;
        long amount = 0;

        assertThatThrownBy(() -> pointController.charge(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("0원 이하 충전 불가");
    }

    @ParameterizedTest(name = "{0}원 충전시 1000원 이하는 충전할수없다.")
    @ValueSource(longs = {700,2000,300})
    void 충전금액이_최소금액_1000원이하이면_예외발생(long amount) {
        long id = 1L;

        assertThatThrownBy(() -> pointController.charge(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최소 충전 금액은 1000원을 초과해야 합니다.");
    }

    @ParameterizedTest(name = "{0}원 충전시 5000원단위로 충전가능하다.")
    @ValueSource(longs = {1000,2000,5000})
    void 충전금액이_5000원단위가_아니면_예외발생(long amount) {
        long id = 1L;

        assertThatThrownBy(() -> pointController.charge(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 5000원 단위여야 합니다.");
    }



    /***
     *  연속으로 테스트 하려고 값 안받았는데 이경우에 설정한 데이터만들어가는데 이렇게해도될까요
     */
    @Test
    void 포인트_사용시_히스토리_확인() {
        long id = 1L;
        //가정 비교 데이터 추가
        userPointTable.insertOrUpdate(id, 2000); //charge 타입

        // 특정 유저가 돈씀
        pointController.use(id, 500);
        pointController.use(id, 1000);

        //히스토리 테이블에 잘즐어갔나
        List<PointHistory> phList = pointHistoryTable.selectAllByUserId(id);

        // 출력 확인
        //phList 중에 내가 넣은거 있는지 확인하기
        assertThat(phList)
                .extracting(ph -> ph.amount(), ph -> ph.type())
                .containsExactlyInAnyOrder(tuple(500L, TransactionType.USE),
                        tuple(1000L, TransactionType.USE)
                );

    }

    /**예외마다 테스트를 진행하는게 맞는지 모르겠습니다.*/
    @Test
    void 하루에_2번이상_사용시_예외_발생() {
        long id = 1L;
        userPointTable.insertOrUpdate(id, 10000);

        // 1차 사용
        pointController.use(id, 300);

        // 2차 사용
        pointController.use(id, 500);

        // 3차 사용 (횟수제한 )
        assertThatThrownBy(() -> pointController.use(id, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("하루에 2번만 사용가능합니다.");
    }

    @ParameterizedTest(name = "{0}원 충전시 최대충전가능은 50000원 충전가능하다.")
    @ValueSource(longs = {1000000,50000})
    void 최대사용금액_50000원_넘으면_예외발생(long amount) {
        long id = 1L;

        assertThatThrownBy(() -> pointController.use(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대사용금액은 50000원 입니다.");
    }
}
