package com.eodegano.cocobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>("200", msg, data);
    }

    public static ApiResponse<Void> ok(String msg) {
        return new ApiResponse<>("200", msg, null);
    }

    public static <T> ApiResponse<T> of(int status, String msg, T data) {
        return new ApiResponse<>(String.valueOf(status), msg, data);
    }
}
