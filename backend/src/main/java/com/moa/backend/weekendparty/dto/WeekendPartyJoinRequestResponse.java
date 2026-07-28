package com.moa.backend.weekendparty.dto;

import com.moa.backend.weekendparty.entity.WeekendPartyMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "대기중(PENDING)인 주말팟 참여 신청 한 건. 파티장이 승인/거절할 때 이 응답의 id를 memberId로 사용한다.")
public record WeekendPartyJoinRequestResponse(
        @Schema(description = "참여 신청 ID. approve/reject API의 {memberId} 경로 변수에 그대로 사용한다.") Long id,
        @Schema(description = "신청자 사용자 ID") Long userId,
        @Schema(description = "신청자 표시 닉네임") String nickname,
        @Schema(description = "신청자 카카오 프로필 사진 URL", nullable = true) String profileImageUrl,
        @Schema(description = "신청한 시각") LocalDateTime requestedAt
) {
    public static WeekendPartyJoinRequestResponse from(WeekendPartyMember member) {
        return new WeekendPartyJoinRequestResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getJoinedAt()
        );
    }
}
