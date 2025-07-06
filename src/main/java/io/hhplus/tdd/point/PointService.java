package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable ;
    private final PointVaildation pointVaildation;

    //테스트 포인트 초기값
    public UserPoint testPoint (long id, long point ) {
        UserPoint userPoint = userPointTable.insertOrUpdate(id,point);
        return userPoint;
    }

    //포인트 조회
    public UserPoint getPoint(long id) {
        //유저 조회시 없으면 0원으로 만들어주기때문에 그냥 리턴
        return userPointTable.selectById(id);
    }

    //히스토리 조회
    public List<PointHistory> getHistory(long id) {
        //포인트 충전내역 조회 최신내역순으로 조회
        return pointHistoryTable.selectAllByUserId(id)
                .stream()
                .sorted(Comparator.comparing(PointHistory::updateMillis).reversed())
                .toList();
    }


    //충전 하기
    public UserPoint charge(long id, long amount) {
        //todo 동시성

        //유저 조회
        UserPoint userPoint = userPointTable.selectById(id);

        //충전 유효성 - 항상양수 , 최소충전 1000, 충전단위 5000
        Map<String,Object> valMap = pointVaildation.validCharge(amount);
        Boolean status = (Boolean) valMap.get("status");
        if(!status){
            throw new IllegalArgumentException(String.valueOf(valMap.get("msg")));
        }

        //최대잔고확인
        if (!userPoint.maxCharge(amount)){
            //log.warn("최대잔고 50만원추가 요청자 - userId: {},amount: {},기존보유금액: {}", id, amount , userPoint.point());
            throw new IllegalArgumentException("최대 잔고 보유는 50만원까지만 가능");
        }

        //유저 포인트 충전
        return userPointTable.insertOrUpdate(id,amount);
    }

    //히스토리 저장하기
    public void saveHistory(UserPoint userPoint, long amount , TransactionType transactionType) {
        long id = userPoint.id();
        long saveTime = userPoint.updateMillis();

        pointHistoryTable.insert(id, amount, transactionType , saveTime);
    }

    //포인트 사용
    public UserPoint use(long id, long amount) {
        //해당하는 유저의 정보 조회
        UserPoint userPoint = userPointTable.selectById(id);

        //충전가능여부 확인 - 들어온 파람 유효성
        List<PointHistory> phList = pointHistoryTable.selectAllByUserId(id); //이력테이블 조회- 하루 충전횟수
        Map<String,Object> valMap = pointVaildation.validUse(phList ,amount);
        Boolean status = (Boolean) valMap.get("status");
        if(!status){
            throw new IllegalArgumentException(String.valueOf(valMap.get("msg")));
        }

        //잔고가 내가 사용할 돈보다 적을때
        log.info("포인트 사용 요청 전 잔액 - : total_amount {}", userPoint.point());

        if(!userPoint.okUse(amount)) {
           // log.warn("잔고부족 사용 요청자 - userId: {}, amount: {}", id, amount);
            throw new IllegalArgumentException("포인트 부족");
        }

        //사용하기


        long useAfterPoint  =  userPoint.use(amount); //남은돈
        UserPoint resUserPoint = userPointTable.insertOrUpdate(id, (useAfterPoint));

       log.info("포인트 사용 요청 후 잔액 - userId: {}, amount: {}, 잔액: {}", id, amount,resUserPoint.point());

        return resUserPoint;
    }
}
