package io.hhplus.tdd.point.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer status;

    // 3개짜리 생성자 명시적으로 추가
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = 200;
    }

    //  성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data);
    }

    //  실패 응답
    public static <T> ApiResponse<T> fail(int status, String message) {
        return new ApiResponse<>(false, message, null, status);
    }


}
