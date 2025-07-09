package io.hhplus.tdd.point.dto;

import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static io.hhplus.tdd.point.common.PointConstants.MAX_USE_POINT;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointUseRequestDto {

    @Max(value = MAX_USE_POINT, message = "1회 최대 사용 금액은 "+MAX_USE_POINT+"원입니다.")
    private long point;

}
