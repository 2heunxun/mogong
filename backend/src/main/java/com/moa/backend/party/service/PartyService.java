package com.moa.backend.party.service;

import com.moa.backend.global.exception.BusinessException;
import com.moa.backend.global.exception.ErrorCode;
import com.moa.backend.party.dto.PartyDetailResponse;
import com.moa.backend.party.dto.PartyMemberResponse;
import com.moa.backend.party.dto.PartyRequest;
import com.moa.backend.party.dto.PartySummaryResponse;
import com.moa.backend.party.entity.PartyMember;
import com.moa.backend.party.entity.StudyParty;
import com.moa.backend.party.repository.PartyMemberRepository;
import com.moa.backend.party.repository.StudyPartyRepository;
import com.moa.backend.party.repository.StudyPartySpecs;
import com.moa.backend.user.entity.User;
import com.moa.backend.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final StudyPartyRepository studyPartyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserService userService;

    public Page<PartySummaryResponse> list(String category, String keyword, StudyParty.Status status, Pageable pageable) {
        var spec = StudyPartySpecs.withFilters(category, keyword, status);
        return studyPartyRepository.findAll(spec, pageable)
                .map(party -> PartySummaryResponse.of(party, partyMemberRepository.countByPartyId(party.getId())));
    }

    public PartyDetailResponse getDetail(Long partyId, Long currentUserId) {
        StudyParty party = getPartyOrThrow(partyId);
        long memberCount = partyMemberRepository.countByPartyId(partyId);
        boolean isOwner = currentUserId != null && party.isOwner(currentUserId);
        boolean isMember = currentUserId != null && partyMemberRepository.existsByPartyIdAndUserId(partyId, currentUserId);
        return PartyDetailResponse.of(party, memberCount, isOwner, isMember);
    }

    public List<PartyMemberResponse> getMembers(Long partyId) {
        StudyParty party = getPartyOrThrow(partyId);
        return partyMemberRepository.findByPartyIdOrderByJoinedAtAsc(partyId).stream()
                .map(member -> PartyMemberResponse.of(member, party.getOwner().getId()))
                .toList();
    }

    @Transactional
    public Long create(Long ownerId, PartyRequest request) {
        User owner = userService.getUserOrThrow(ownerId);

        StudyParty party = studyPartyRepository.save(StudyParty.builder()
                .owner(owner)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .capacity(request.capacity())
                .openChatUrl(request.openChatUrl())
                .build());

        partyMemberRepository.save(PartyMember.builder()
                .party(party)
                .user(owner)
                .build());

        return party.getId();
    }

    @Transactional
    public void update(Long partyId, Long userId, PartyRequest request) {
        StudyParty party = getPartyOrThrow(partyId);
        party.validateOwner(userId);
        party.update(request.title(), request.description(), request.category(), request.capacity(), request.openChatUrl());
    }

    @Transactional
    public void delete(Long partyId, Long userId) {
        StudyParty party = getPartyOrThrow(partyId);
        party.validateOwner(userId);
        studyPartyRepository.delete(party);
    }

    @Transactional
    public void join(Long partyId, Long userId) {
        StudyParty party = getPartyOrThrow(partyId);
        User user = userService.getUserOrThrow(userId);

        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.PARTY_CLOSED);
        }
        if (partyMemberRepository.existsByPartyIdAndUserId(partyId, userId)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }
        if (partyMemberRepository.countByPartyId(partyId) >= party.getCapacity()) {
            throw new BusinessException(ErrorCode.PARTY_FULL);
        }

        partyMemberRepository.save(PartyMember.builder().party(party).user(user).build());
    }

    @Transactional
    public void leave(Long partyId, Long userId) {
        StudyParty party = getPartyOrThrow(partyId);

        if (party.isOwner(userId)) {
            throw new BusinessException(ErrorCode.OWNER_CANNOT_LEAVE);
        }
        if (!partyMemberRepository.existsByPartyIdAndUserId(partyId, userId)) {
            throw new BusinessException(ErrorCode.NOT_JOINED);
        }

        partyMemberRepository.deleteByPartyIdAndUserId(partyId, userId);
    }

    private StudyParty getPartyOrThrow(Long partyId) {
        return studyPartyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));
    }
}
