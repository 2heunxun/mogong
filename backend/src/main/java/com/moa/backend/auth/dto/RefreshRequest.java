package com.moa.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "액세스 토큰 재발급 요청")
public record RefreshRequest(
        @Schema(description = "로그인 시 함께 발급받은 리프레시 토큰")
        @NotBlank(message = "리프레시 토큰은 필수입니다.") String refreshToken
) {
}
