package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.domain.UserPoint;
public record UserPointResponseDto(
        long id,
        long point,
        long updateMillis
) {
 //타입변환용 from 메서드
    public static UserPointResponseDto from(UserPoint userPoint) {
        return new UserPointResponseDto(
                userPoint.id(),
                userPoint.point(),
                userPoint.updateMillis()
        );
    }
}