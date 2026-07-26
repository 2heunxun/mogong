package com.moa.backend.party.dto;

import com.moa.backend.party.entity.StudyParty;

public record PartySummaryResponse(
        Long id,
        String title,
        String category,
        Integer capacity,
        long memberCount,
        String status,
        String ownerNickname,
        java.time.LocalDateTime createdAt
) {
    public static PartySummaryResponse of(StudyParty party, long memberCount) {
        return new PartySummaryResponse(
                party.getId(),
                party.getTitle(),
                party.getCategory(),
                party.getCapacity(),
                memberCount,
                party.getStatus().name(),
                party.getOwner().getNickname(),
                party.getCreatedAt()
        );
    }
}
