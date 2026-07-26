package com.moa.backend.auth.dto;

import com.moa.backend.user.dto.UserResponse;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
