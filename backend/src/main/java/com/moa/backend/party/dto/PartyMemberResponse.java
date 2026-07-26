package com.moa.backend.party.dto;

import com.moa.backend.party.entity.PartyMember;
import java.time.LocalDateTime;

public record PartyMemberResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        boolean isOwner,
        LocalDateTime joinedAt
) {
    public static PartyMemberResponse of(PartyMember member, Long ownerId) {
        return new PartyMemberResponse(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getUser().getId().equals(ownerId),
                member.getJoinedAt()
        );
    }
}
