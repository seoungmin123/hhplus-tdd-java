package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.PointValidation;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable ;
    private final PointValidation pointVaildation;


    //포인트 조회
    @Override
    public UserPoint getPoint(long id) {
        //유저 조회시 없으면 0원으로 만들어주기때문에 그냥 리턴 -유저가 없는경우 고려x
        return userPointTable.selectById(id);
    }

    //히스토리 조회
    @Override
    public List<PointHistory> getHistory(long id) {
        //포인트 충전내역 조회 최신내역순으로 조회
        List<PointHistory> phlist = pointHistoryTable.selectAllByUserId(id);
        return phlist
                .stream()
                .sorted(Comparator.comparing(PointHistory::updateMillis).reversed())
                .toList();
    }

    //포인트 사용
    @Transactional
    @Override
    public UserPoint usePoint(long id, long amount) {
        // 사용자 조회
        UserPoint now = userPointTable.selectById(id);

        //  히스토리 기반 검증
        List<PointHistory> history = pointHistoryTable.selectAllByUserId(id);
        pointVaildation.validUse(history, amount);

        // 도메인 내부 검증 + 업데이트
        UserPoint updated = now.useAndUpdate(amount);

        // DB 반영 & 이력 저장
        userPointTable.insertOrUpdate(id, updated.point());
        saveHistory(updated, amount, TransactionType.USE);

        return updated;
    }


    //포인트 충전
    @Transactional
    @Override
    public UserPoint useCharge(long id, long amount) {
        //사용자조회
        UserPoint now = userPointTable.selectById(id);

        // 도메인 내부 검증 + 업데이트
        UserPoint updated = now.chargeAndUpdate(amount);

        userPointTable.insertOrUpdate(id, updated.point());
        saveHistory(updated, amount, TransactionType.CHARGE);

        return updated;
    }

    //히스토리 저장하기
    @Override
    public void saveHistory(UserPoint userPoint, long amount , TransactionType transactionType) {
        long id = userPoint.id();
        long saveTime = userPoint.updateMillis();

        pointHistoryTable.insert(id, amount, transactionType , saveTime);
    }

}



/// 질문
//TODO 중복된 코드를 리팩토링하다보니 아래의 주석코드처럼 수정했었습니다. 근데 이렇게되면 테스트도 복잡성이 늘어나서
//의도적으로 중복된 코드를 가지게 수정을 하였는데 만일 충전, 사용 뿐만아니라 다른 비슷한 메서드들이 늘어나게되면 중복코드가 너무 늘어나는것은 아닌지 의문입니다.
    /*
    //포인트 사용 - 후 히스토리 기록
    @Transactional
    @Override
    public UserPoint usePoint(long id, long amount) {
        // 잔액 차감
        UserPoint userPoint = adjustmentPoint(
                id
                ,amount
                // 검증
                ,(userPointV0, amt) -> {
                                                        List<PointHistory> phList = pointHistoryTable.selectAllByUserId(id);
                                                        pointVaildation.validUse(phList,amt); //서비스 입력값 자체
                    return userPointV0.useAndUpdate(amt); //기본 사용 관련 유효성 (도메인 자체검증 후 새객체 리턴);
                }
               // ,UserPoint::useAndUpdate //UserPoint에서 객체 새로
        );

        // 이력 저장
        saveHistory(userPoint, amount, TransactionType.USE);
        return userPoint;
    }

    //포인트 충전- 후 히스토리 기록
    @Transactional
    @Override
    public UserPoint useCharge(long id, long amount) {
        // 잔액 충전
        //기본 충전 관련 유효성 (도메인 자체검증)
        UserPoint userPoint =adjustmentPoint(
                id
                ,amount
                , UserPoint::chargeAndUpdate
               // ,UserPoint::chargeAndUpdate
        );
        // 이력 저장
        saveHistory(userPoint, amount, TransactionType.CHARGE);
        return userPoint;
    }

    //충전, 사용 할 포인트 조절 , 검증
    @Override
    public UserPoint adjustmentPoint(
            long id, //아이디
            long amount, //충전 or 사용 할 포인트
            //BiConsumer<UserPoint, Long> valid, //검증만하기  (up, amt) -> void : 검증
            BiFunction<UserPoint, Long, UserPoint> update//변경만하기  (up, amt) -> UserPoint : 상태 변경 리턴
    ) {
        // 특정유저조회
        UserPoint nowUserPoint = userPointTable.selectById(id);

        // 검증ㅁ만 - void
       // valid.accept(nowUserPoint, amount);

        // 포인트 값 변경 UserPoint (useAndUpdate / chargeAndUpdate)
        UserPoint newUserPoint = update.apply(nowUserPoint, amount);

        // 테이블 값 변경하기 (point만)
        userPointTable.insertOrUpdate(id, newUserPoint.point());

        return newUserPoint;
    }
*/
