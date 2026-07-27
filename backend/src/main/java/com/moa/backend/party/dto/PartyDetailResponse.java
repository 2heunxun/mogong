package com.moa.backend.party.dto;

import com.moa.backend.party.entity.PartyMember;
import com.moa.backend.party.entity.StudyParty;
import com.moa.backend.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "파티 상세 정보")
public record PartyDetailResponse(
        @Schema(description = "파티 ID") Long id,
        @Schema(description = "파티 제목") String title,
        @Schema(description = "파티 소개", nullable = true) String description,
        @Schema(description = "카테고리", example = "코딩테스트") String category,
        @Schema(description = "모집 정원") Integer capacity,
        @Schema(description = "현재 승인된(APPROVED) 파티원 수") long memberCount,
        @Schema(description = "모집 상태", allowableValues = {"RECRUITING", "CLOSED"}) String status,
        @Schema(description = "파티장 정보") UserResponse owner,

        @Schema(description = "요청자 본인이 이 파티의 파티장인지 여부. 비로그인 요청이면 항상 false.")
        boolean isOwner,

        @Schema(description = """
                요청자 본인의 참여 상태. 비로그인 요청이거나 한 번도 신청한 적 없으면 null.
                - `PENDING`: 참여 신청 후 파티장 승인 대기중
                - `APPROVED`: 참여 승인됨 (정식 파티원)
                - `REJECTED`: 파티장이 신청을 거절함
                """, nullable = true, allowableValues = {"PENDING", "APPROVED", "REJECTED"})
        String memberStatus,

        @Schema(description = "카카오 오픈채팅 링크. 파티장 본인이거나 memberStatus=APPROVED일 때만 값이 채워지고, 그 외에는 null.",
                nullable = true)
        String openChatUrl,

        @Schema(description = "파티 생성 시각") LocalDateTime createdAt,
        @Schema(description = "파티 정보 마지막 수정 시각") LocalDateTime updatedAt
) {
    public static PartyDetailResponse of(StudyParty party, long memberCount, boolean isOwner, PartyMember.Status memberStatus) {
        boolean canSeeOpenChat = isOwner || memberStatus == PartyMember.Status.APPROVED;
        return new PartyDetailResponse(
                party.getId(),
                party.getTitle(),
                party.getDescription(),
                party.getCategory(),
                party.getCapacity(),
                memberCount,
                party.getStatus().name(),
                UserResponse.from(party.getOwner()),
                isOwner,
                memberStatus == null ? null : memberStatus.name(),
                canSeeOpenChat ? party.getOpenChatUrl() : null,
                party.getCreatedAt(),
                party.getUpdatedAt()
        );
    }
}
