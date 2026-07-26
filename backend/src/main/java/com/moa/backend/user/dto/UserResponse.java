package com.moa.backend.user.dto;

import com.moa.backend.user.entity.User;

public record UserResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole().name()
        );
    }
}
