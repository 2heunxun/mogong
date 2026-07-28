package com.moa.backend.weekendparty.dto;

import com.moa.backend.weekendparty.entity.WeekendPartyMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "승인된(APPROVED) 주말팟 파티원 한 명의 정보")
public record WeekendPartyMemberResponse(
        @Schema(description = "사용자 ID") Long userId,
        @Schema(description = "표시 닉네임 (온보딩 완료 후에는 N반_N조_실명 형식)") String nickname,
        @Schema(description = "카카오 프로필 사진 URL", nullable = true) String profileImageUrl,
        @Schema(description = "이 파티원이 파티장인지 여부") boolean isOwner,
        @Schema(description = "참여 승인된 시각") LocalDateTime joinedAt
) {
    public static WeekendPartyMemberResponse of(WeekendPartyMember member, Long ownerId) {
        return new WeekendPartyMemberResponse(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getUser().getId().equals(ownerId),
                member.getJoinedAt()
        );
    }
}
