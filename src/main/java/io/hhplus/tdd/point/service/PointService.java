package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.common.PointValidation;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static io.hhplus.tdd.point.common.PointConstants.MAX_DAILY_USE_COUNT;
import static io.hhplus.tdd.point.common.PointConstants.getTodayStartMillis;
import static io.hhplus.tdd.point.exception.PointErrorCode.ERR_DAILY_USE_LIMIT_EXCEEDED;

@Service
@RequiredArgsConstructor
public class PointService  {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable ;
    private final PointValidation pointValidation ;


    //포인트 조회
    public UserPoint getPoint(long id) {
        //유저 조회시 없으면 0원으로 만들어주기때문에 그냥 리턴 -유저가 없는경우 고려x
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

    //포인트 사용
    @Transactional
    public UserPoint usePoint(long id, long amount) {
        // 사용자 조회
        UserPoint now = userPointTable.selectById(id);

        //히스토리 기반 검증
        List<PointHistory> history = pointHistoryTable.selectAllByUserId(id);
        long cnt = pointValidation.validUse(history, TransactionType.USE , getTodayStartMillis());

        log.debug("[요청] 포인트 사용 횟수검증 : userId={}, cnt={}", id, cnt);

        if(cnt >= MAX_DAILY_USE_COUNT ){ // 0회 사용전 , 첫 사용후, 두번째부터 안되야함
            throw new PointException(ERR_DAILY_USE_LIMIT_EXCEEDED);//하루 2번사용
        }

        log.debug("[요청] 포인트 사용 잔고 확인  : userId={}, point={}", id, now.point());

        // 도메인 내부 검증 + 업데이트
        PointErrorCode errorCd = now.useValid(amount);
        if(errorCd != null) {
            throw new PointException(errorCd);
        }

        // DB 반영 & 이력 저장
        UserPoint updated = userPointTable.insertOrUpdate(id, now.point() - amount);
        saveHistory(updated, amount, TransactionType.USE);
        return updated;
    }


    //포인트 충전
    @Transactional
    public UserPoint useCharge(long id, long amount) {
        //사용자조회
        UserPoint now = userPointTable.selectById(id);

        log.debug("[요청] 포인트 충전 금액 확인  : userId={}, amount={}", id, amount);
        log.debug("[요청] 포인트 충전 잔고 확인  : userId={}, point={}", id, now.point());

        // 도메인 내부 검증 + 업데이트
        PointErrorCode errorCd = now.chargeValid(amount);
        if(errorCd != null) {
            throw new PointException(errorCd);
        }

        // DB 반영 & 이력 저장
        UserPoint updated = userPointTable.insertOrUpdate(id, now.point() + amount);
        saveHistory(updated, amount, TransactionType.CHARGE);

        return updated;
    }

    //히스토리 저장하기
    public void saveHistory(UserPoint userPoint, long amount , TransactionType transactionType) {
        long id = userPoint.id();
        long saveTime = userPoint.updateMillis();

        pointHistoryTable.insert(id, amount, transactionType , saveTime);
    }


}

