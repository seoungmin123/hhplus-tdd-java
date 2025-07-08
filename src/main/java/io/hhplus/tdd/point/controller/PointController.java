package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.service.PointServiceImpl;
import io.hhplus.tdd.point.common.ApiResponse;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointServiceImpl pointServiceImpl;


    /**
     *특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public ApiResponse<UserPoint> point(
            @PathVariable long id
    ) {
        UserPoint userPoint = pointServiceImpl.getPoint(id);
        return ApiResponse.success(userPoint);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public ApiResponse<List<PointHistory>> history(
            @PathVariable long id
    ) {
        List<PointHistory> phList = pointServiceImpl.getHistory(id);
        return ApiResponse.success(phList);
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public ApiResponse<UserPoint> charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        //충전
        UserPoint userPoint = pointServiceImpl.useCharge(id,amount);

        return ApiResponse.success(userPoint);
    }

    /**
     * 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */

    @PatchMapping("{id}/use")
    public ApiResponse<UserPoint> use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        //사용하기
        UserPoint userPoint = pointServiceImpl.usePoint(id,amount);

        return ApiResponse.success(userPoint);
    }
}
