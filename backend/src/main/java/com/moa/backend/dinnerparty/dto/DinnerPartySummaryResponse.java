package com.moa.backend.dinnerparty.dto;

import com.moa.backend.dinnerparty.entity.DinnerParty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "저녁팟 목록/내 저녁팟 목록에 쓰이는 요약 정보")
public record DinnerPartySummaryResponse(
        @Schema(description = "저녁팟 ID") Long id,
        @Schema(description = "저녁팟 제목") String title,
        @Schema(description = "식사 장소") String location,
        @Schema(description = "저녁 약속 일시") LocalDateTime meetingAt,
        @Schema(description = "모집 정원") Integer capacity,
        @Schema(description = "현재 승인된(APPROVED) 파티원 수. 대기중인 신청자는 포함하지 않는다.") long memberCount,
        @Schema(description = "모집 상태", allowableValues = {"RECRUITING", "CLOSED"}) String status,
        @Schema(description = "모집 방식", allowableValues = {"FIRST_COME", "APPROVAL"}) String recruitmentType,
        @Schema(description = "파티장 닉네임") String ownerNickname,
        @Schema(description = "생성 시각") LocalDateTime createdAt
) {
    public static DinnerPartySummaryResponse of(DinnerParty party, long memberCount) {
        return new DinnerPartySummaryResponse(
                party.getId(),
                party.getTitle(),
                party.getLocation(),
                party.getMeetingAt(),
                party.getCapacity(),
                memberCount,
                party.getStatus().name(),
                party.getRecruitmentType().name(),
                party.getOwner().getNickname(),
                party.getCreatedAt()
        );
    }
}
