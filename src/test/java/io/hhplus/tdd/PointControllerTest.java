
package io.hhplus.tdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.exception.ApiControllerAdvice;
import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ApiControllerAdvice.class)
@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean
    PointService pointService;

    @Autowired
    ObjectMapper objectMapper;  // DTO JSON 변환용

// 컨트롤러에서 호출이 잘되는지 (원하는 응답형식으로 리턴되나)

    @Test
    @DisplayName("포인트사용시  입력값 유효성  검증")
    void 포인트사용_입력값_검증() throws Exception {
        long id = 1L;
        long amount = 250L;
        // 서비스는 아무 UserPoint든 리턴하도록 stub
        given(pointService.usePoint(anyLong(), anyLong()))
                .willReturn(new UserPoint(id, 1000L,System.currentTimeMillis()));

        mockMvc.perform(
                        patch("/point/{id}/use", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.valueOf(amount))
                )
                .andExpect(status().isOk());

        // id와 amount가 정확히 서비스에 전달되었는지 확인
        then(pointService).should(times(1)).usePoint(id, amount);
    }

    @Test
    @DisplayName("포인트 사용 응답 JSON의 필드 값이 예상과 일치하는지 검증")
    void 포인트_사용_응답값_검증() throws Exception {
        long id = 42L;
        long 쓴돈 = 300L;
        long 초기돈 = 700L;
        UserPoint dummyResponse = new UserPoint(id, 초기돈,System.currentTimeMillis());
        given(pointService.usePoint(id, 쓴돈))
                .willReturn(dummyResponse);

        mockMvc.perform(
                        patch("/point/{id}/use", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.valueOf(쓴돈))
                )
                .andExpect(status().isOk())
                // ApiResponse.success()로 감싼 data.userId, data.point 확인
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.point").value(초기돈));
    }

    @Test
    @DisplayName("유저 호인트 조회 GET /point/{id} 성공 응답")
    void 유저_포인트_조회_성공() throws Exception {
        //stub 행위검증 성공여부
        UserPoint stub = new UserPoint(1, 12345, System.currentTimeMillis());
        given(pointService.getPoint(1L)).willReturn(stub); //mock으로 반환하기

        mockMvc.perform(get("/point/1")) //요청
                .andExpect(status().isOk()) //상대값
                .andExpect(jsonPath("$.success").value(true)) //성공여부
                .andExpect(jsonPath("$.data.point").value(12345)); //값 검증
    }

    @Test
    @DisplayName("유저 히스토리 조회 GET /point/{id}/histories 성공 응답")
    void 유저_히스토리_조회() throws Exception {
        PointHistory h1 = new PointHistory(1, 1, 500L, TransactionType.USE, System.currentTimeMillis());
        PointHistory h2 = new PointHistory(2, 1, 2000L, TransactionType.CHARGE, System.currentTimeMillis());
        given(pointService.getHistory(1L)).willReturn(List.of(h1, h2));

        mockMvc.perform(get("/point/1/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[?(@.amount == 500)].type").value("USE"))
                .andExpect(jsonPath("$.data[?(@.amount == 2000)].type").value("CHARGE"));
    }

    @Test
    @DisplayName("유저 포인트 충전 PATCH /point/{id}/charge 성공 응답")
    void 유저_포인트_충전_성공() throws Exception {
        UserPoint stub =  new UserPoint(1, 70000, System.currentTimeMillis());
        given(pointService.useCharge(1L, 5000L)).willReturn(stub);


        // JSON 바디로 변환
        String requestJson = objectMapper.writeValueAsString(Map.of("point", 5000));


        mockMvc.perform(patch("/point/1/charge")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.point").value(70000));//어차피 mock이라 안바뀜
    }

    @Test
    @DisplayName("유저 포인트 사용 PATCH /point/{id}/use 성공 응답")
    void 유저_포인트_사용_성공() throws Exception {
        UserPoint stub = new UserPoint(1, 70000, System.currentTimeMillis());
        given(pointService.usePoint(1L, 5000L)).willReturn(stub);

        // JSON 바디로 변환
        String requestJson = objectMapper.writeValueAsString(Map.of("point", 5000));


        mockMvc.perform(patch("/point/1/use")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.point").value(70000)); //어차피 mock이라 안바뀜
    }



}
