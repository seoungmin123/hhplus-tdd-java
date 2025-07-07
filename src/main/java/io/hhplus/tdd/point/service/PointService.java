package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.PointValidation;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.common.ApiResponse;
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
public class PointService {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable ;
    private final PointValidation pointVaildation;


    //포인트 조회
    public UserPoint getPoint(long id) {
        //유저 조회시 없으면 0원으로 만들어주기때문에 그냥 리턴
        return userPointTable.selectById(id);
    }

    //히스토리 조회
    public List<PointHistory> getHistory(long id) {
        //포인트 충전내역 조회 최신내역순으로 조회
        List<PointHistory> phlist = pointHistoryTable.selectAllByUserId(id);
        return phlist
                .stream()
                .sorted(Comparator.comparing(PointHistory::updateMillis).reversed())
                .toList();
    }

    //포인트 사용 - 후 히스토리 기록
    @Transactional
    public UserPoint usePoint(long id, long amount) {
        // 잔액 차감
        UserPoint userPoint = useNchargePoint(
                id
                ,amount
                // 검증
                ,(userPointV0, amt) -> {
                                                        List<PointHistory> phList = pointHistoryTable.selectAllByUserId(id);
                                                        pointVaildation.validUse(phList,amt);
                                                        userPointV0.okUse(amt); //잔액부족
                }
                ,UserPoint::useAndUpdate //UserPoint에서 객체 새로
        );

        // 이력 저장
        saveHistory(userPoint, amount, TransactionType.USE);
        return userPoint;
    }

    //포인트 충전- 후 히스토리 기록
    @Transactional
    public UserPoint useCharge(long id, long amount) {
        // 잔액 충전
        UserPoint userPoint =useNchargePoint(
                id
                ,amount
                ,(userPointV0, amt) -> {
                    pointVaildation.validCharge(amt);
                    userPointV0.maxCharge(amt); //최대잔고
                }
                ,UserPoint::chargeAndUpdate
        );
        // 이력 저장
        saveHistory(userPoint, amount, TransactionType.CHARGE);
        return userPoint;
    }

    //충전, 사용 할 포인트 조절 , 검증
    private UserPoint useNchargePoint(
            long id, //아이디
            long amount, //충전 or 사용 할 포인트
            BiConsumer<UserPoint, Long> valid, //검증만하기  (up, amt) -> void : 검증
            BiFunction<UserPoint, Long, UserPoint> update//변경만하기  (up, amt) -> UserPoint : 상태 변경 리턴
    ) {
        // 특정유저조회
        UserPoint nowUserPoint = userPointTable.selectById(id);

        // 검증ㅁ만 - void
        valid.accept(nowUserPoint, amount);

        // 포인트 값 변경 UserPoint (useAndUpdate / chargeAndUpdate)
        UserPoint newUserPoint = update.apply(nowUserPoint, amount);

        // 테이블 값 변경하기 (point만)
        userPointTable.insertOrUpdate(id, newUserPoint.point());

        return newUserPoint;
    }


    //히스토리 저장하기
    public void saveHistory(UserPoint userPoint, long amount , TransactionType transactionType) {
        long id = userPoint.id();
        long saveTime = userPoint.updateMillis();

        pointHistoryTable.insert(id, amount, transactionType , saveTime);
    }

}
