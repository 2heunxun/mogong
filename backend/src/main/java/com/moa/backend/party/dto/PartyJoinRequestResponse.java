package com.moa.backend.party.dto;

import com.moa.backend.party.entity.PartyMember;
import java.time.LocalDateTime;

public record PartyJoinRequestResponse(
        Long id,
        Long userId,
        String nickname,
        String profileImageUrl,
        LocalDateTime requestedAt
) {
    public static PartyJoinRequestResponse from(PartyMember member) {
        return new PartyJoinRequestResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getJoinedAt()
        );
    }
}
