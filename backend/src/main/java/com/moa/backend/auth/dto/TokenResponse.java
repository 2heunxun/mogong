package com.moa.backend.auth.dto;

import com.moa.backend.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인/토큰 재발급 응답")
public record TokenResponse(
        @Schema(description = "API 요청 시 `Authorization: Bearer {accessToken}` 헤더로 사용. 기본 30분 만료.")
        String accessToken,

        @Schema(description = "accessToken 만료 시 `POST /api/auth/refresh`에 사용. 기본 14일 만료, 재발급 시마다 회전됨.")
        String refreshToken,

        @Schema(description = "로그인한 사용자 정보. `profileCompleted = false`이면 온보딩(반/조/이름 등록)을 먼저 완료해야 한다.")
        UserResponse user
) {
}
