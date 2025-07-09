package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.exception.PointErrorMsg;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointChargeRequestDto {
    @PositiveOrZero(message = PointErrorMsg.MSG_BELOW_MINIMUM_CHARGE)
    private long point;

}
