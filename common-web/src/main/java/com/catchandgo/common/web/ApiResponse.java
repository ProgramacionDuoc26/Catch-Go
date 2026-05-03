package com.catchandgo.common.web;

public record ApiResponse<T>(String message, T data) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(message, data);
    }
}
