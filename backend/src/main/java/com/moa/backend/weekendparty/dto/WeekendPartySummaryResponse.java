package com.moa.backend.weekendparty.dto;

import com.moa.backend.weekendparty.entity.WeekendParty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "주말팟 목록/내 주말팟 목록에 쓰이는 요약 정보")
public record WeekendPartySummaryResponse(
        @Schema(description = "주말팟 ID") Long id,
        @Schema(description = "주말팟 제목") String title,
        @Schema(description = "카테고리", example = "코딩테스트") String category,
        @Schema(description = "주말 모임 일시") LocalDateTime meetingAt,
        @Schema(description = "모집 정원") Integer capacity,
        @Schema(description = "현재 승인된(APPROVED) 파티원 수. 대기중인 신청자는 포함하지 않는다.") long memberCount,
        @Schema(description = "모집 상태", allowableValues = {"RECRUITING", "CLOSED"}) String status,
        @Schema(description = "파티장 닉네임") String ownerNickname,
        @Schema(description = "생성 시각") LocalDateTime createdAt
) {
    public static WeekendPartySummaryResponse of(WeekendParty party, long memberCount) {
        return new WeekendPartySummaryResponse(
                party.getId(),
                party.getTitle(),
                party.getCategory(),
                party.getMeetingAt(),
                party.getCapacity(),
                memberCount,
                party.getStatus().name(),
                party.getOwner().getNickname(),
                party.getCreatedAt()
        );
    }
}
