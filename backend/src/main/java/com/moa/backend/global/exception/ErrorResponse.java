package com.moa.backend.global.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        List<FieldErrorDetail> fieldErrors
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(LocalDateTime.now(), errorCode.getStatus().value(), errorCode.name(), message, null);
    }

    public static ErrorResponse of(int status, String code, String message, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, code, message, fieldErrors);
    }

    public record FieldErrorDetail(String field, String reason) {
    }
}
