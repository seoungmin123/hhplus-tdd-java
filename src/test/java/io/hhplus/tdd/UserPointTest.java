package io.hhplus.tdd;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.hhplus.tdd.point.exception.PointErrorCode.ERR_EXCEED_BALANCE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class UserPointTest {

    /**
     * 의도!!!  실제객체를 사용한 도메인 테스트 -> 응답값 중심 테스트를 하고싶었음
     * */

    private UserPoint up;
    @BeforeEach
    void init() {
        up = new UserPoint(1, 90_000L, 0);
    }

    @Test
    void 최초사용자_포인트_0원() { //순수자바객체로 서비스
        // given

        long userId = 77L;
        // when
        UserPoint up = UserPoint.empty(userId);
        // then
        assertThat(up.id()).isEqualTo(userId);
        assertThat(up.point()).isZero();
    }

    @ParameterizedTest
    @ValueSource(longs = { 1000000})
    void 보유잔고_초과시_에러(long amt) {//보유잔고가 십만원 초과할때
        //when
        PointErrorCode err = up.chargeValid(amt);

        // then
        assertThat(err).isEqualTo(ERR_EXCEED_BALANCE);
    }



}
