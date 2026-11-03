package com.example.campusshop.common;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiError", description = "A consistent error payload returned by the API.")
public record ApiError(
        String code,
        String message,
        List<String> details) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError withDetails(String code, String message, List<String> details) {
        return new ApiError(code, message, details);
    }
}
