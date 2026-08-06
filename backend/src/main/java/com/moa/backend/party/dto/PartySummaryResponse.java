package com.moa.backend.party.dto;

import com.moa.backend.party.entity.StudyParty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "파티 목록/내 파티 목록에 쓰이는 요약 정보")
public record PartySummaryResponse(
        @Schema(description = "파티 ID") Long id,
        @Schema(description = "파티 제목") String title,
        @Schema(description = "카테고리", example = "코딩테스트") String category,
        @Schema(description = "모집 정원") Integer capacity,
        @Schema(description = "현재 승인된(APPROVED) 파티원 수. 대기중인 신청자는 포함하지 않는다.") long memberCount,
        @Schema(description = "모집 상태", allowableValues = {"RECRUITING", "CLOSED"}) String status,
        @Schema(description = "모집 방식", allowableValues = {"FIRST_COME", "APPROVAL"}) String recruitmentType,
        @Schema(description = "파티장 닉네임") String ownerNickname,
        @Schema(description = "생성 시각") LocalDateTime createdAt
) {
    public static PartySummaryResponse of(StudyParty party, long memberCount) {
        return new PartySummaryResponse(
                party.getId(),
                party.getTitle(),
                party.getCategory(),
                party.getCapacity(),
                memberCount,
                party.getStatus().name(),
                party.getRecruitmentType().name(),
                party.getOwner().getNickname(),
                party.getCreatedAt()
        );
    }
}
