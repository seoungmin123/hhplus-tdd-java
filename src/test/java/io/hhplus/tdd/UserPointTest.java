package io.hhplus.tdd;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.hhplus.tdd.point.exception.PointErrorCode.ERR_BELOW_MINIMUM_CHARGE;
import static io.hhplus.tdd.point.exception.PointErrorCode.ERR_INSUFFICIENT_BALANCE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class UserPointTest {

    private UserPoint up;
    @BeforeEach
    void init() {
        up = new UserPoint(1, 100, 0);
    }

    @Test
    void 사용자_없으면_포인트_0원() {
        // given
        long userId = 77L;
        // when
        UserPoint up = UserPoint.empty(userId);
        // then
        assertThat(up.id()).isEqualTo(userId);
        assertThat(up.point()).isZero();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -100})
    void 도메은_충전시_에러확인(long amt) {//양수가 아닐경우
        assertThatThrownBy(() -> up.chargeAndUpdate(amt))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(ERR_BELOW_MINIMUM_CHARGE);
    }

    @Test
    void 도메인_사용_검증_확인() { //충전잔고 확인 에러
        assertThatThrownBy(() -> up.okUse(200))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(ERR_INSUFFICIENT_BALANCE);
    }

}
