package com.moa.backend.user.dto;

import com.moa.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보")
public record UserResponse(
        @Schema(description = "사용자 ID") Long id,

        @Schema(description = """
                표시 닉네임. 온보딩(가입 완료) 전에는 카카오 닉네임 그대로이고,
                온보딩 완료 후에는 `N반_N조_실명` 형식으로 고정된다.
                """, example = "3반_2조_홍길동")
        String nickname,

        @Schema(description = "카카오 프로필 사진 URL. 없을 수 있음(null 가능).", nullable = true) String profileImageUrl,

        @Schema(description = "권한. 대부분의 사용자는 USER.", example = "USER", allowableValues = {"USER", "ADMIN"}) String role,

        @Schema(description = "소속 반 (1~10). 온보딩 전이면 null.", example = "3", nullable = true) Integer classNo,

        @Schema(description = "소속 조. 온보딩 전이면 null.", example = "2", nullable = true) Integer teamNo,

        @Schema(description = "실명. 온보딩 전이면 null.", example = "홍길동", nullable = true) String realName,

        @Schema(description = "회원가입(온보딩) 완료 여부. false면 파티 생성/참여 신청이 모두 차단된다.") boolean profileCompleted
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getClassNo(),
                user.getTeamNo(),
                user.getRealName(),
                user.isProfileCompleted()
        );
    }
}
