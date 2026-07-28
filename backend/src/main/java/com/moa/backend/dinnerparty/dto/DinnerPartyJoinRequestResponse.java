package com.moa.backend.dinnerparty.dto;

import com.moa.backend.dinnerparty.entity.DinnerPartyMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "대기중(PENDING)인 저녁팟 참여 신청 한 건. 파티장이 승인/거절할 때 이 응답의 id를 memberId로 사용한다.")
public record DinnerPartyJoinRequestResponse(
        @Schema(description = "참여 신청 ID. approve/reject API의 {memberId} 경로 변수에 그대로 사용한다.") Long id,
        @Schema(description = "신청자 사용자 ID") Long userId,
        @Schema(description = "신청자 표시 닉네임") String nickname,
        @Schema(description = "신청자 카카오 프로필 사진 URL", nullable = true) String profileImageUrl,
        @Schema(description = "신청한 시각") LocalDateTime requestedAt
) {
    public static DinnerPartyJoinRequestResponse from(DinnerPartyMember member) {
        return new DinnerPartyJoinRequestResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getJoinedAt()
        );
    }
}
