package com.moa.backend.party.dto;

import com.moa.backend.party.entity.PartyMember;
import com.moa.backend.party.entity.StudyParty;
import com.moa.backend.user.dto.UserResponse;
import java.time.LocalDateTime;

public record PartyDetailResponse(
        Long id,
        String title,
        String description,
        String category,
        Integer capacity,
        long memberCount,
        String status,
        UserResponse owner,
        boolean isOwner,
        String memberStatus,
        String openChatUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
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
