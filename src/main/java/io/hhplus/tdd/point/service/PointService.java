package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;

import java.util.List;

public interface PointService {

    UserPoint getPoint(long id) ; //포인트 조회

    List<PointHistory> getHistory(long id); //히스토리 조회

    UserPoint usePoint(long id, long amount) ;//포인트 사용 - 후 히스토리 기록

    UserPoint useCharge(long id, long amount) ; //포인트 충전- 후 히스토리 기록

    /* 리팩토링시 수정 함 주석처리
    UserPoint adjustmentPoint(long id, long amount //, BiConsumer<UserPoint, Long> valid
            ,BiFunction<UserPoint, Long, UserPoint> update); //충전, 사용 할 포인트 조절 , 검증

     */

    void saveHistory(UserPoint userPoint, long amount , TransactionType transactionType) ;//히스토리 저장하기
}
