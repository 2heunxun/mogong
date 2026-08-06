package com.moa.backend.party.service;

import com.moa.backend.global.exception.BusinessException;
import com.moa.backend.global.exception.ErrorCode;
import com.moa.backend.party.dto.MyPartiesResponse;
import com.moa.backend.party.dto.PartyDetailResponse;
import com.moa.backend.party.dto.PartyJoinRequestResponse;
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
                .map(this::toSummary);
    }

    public PartyDetailResponse getDetail(Long partyId, Long currentUserId) {
        StudyParty party = getPartyOrThrow(partyId);
        long memberCount = partyMemberRepository.countByPartyIdAndStatus(partyId, PartyMember.Status.APPROVED);
        boolean isOwner = currentUserId != null && party.isOwner(currentUserId);
        PartyMember.Status memberStatus = currentUserId == null
                ? null
                : partyMemberRepository.findByPartyIdAndUserId(partyId, currentUserId).map(PartyMember::getStatus).orElse(null);
        return PartyDetailResponse.of(party, memberCount, isOwner, memberStatus);
    }

    public List<PartyMemberResponse> getMembers(Long partyId) {
        StudyParty party = getPartyOrThrow(partyId);
        return partyMemberRepository.findByPartyIdAndStatusOrderByJoinedAtAsc(partyId, PartyMember.Status.APPROVED).stream()
                .map(member -> PartyMemberResponse.of(member, party.getOwner().getId()))
                .toList();
    }

    public List<PartyJoinRequestResponse> getJoinRequests(Long partyId, Long ownerId) {
        StudyParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        return partyMemberRepository.findByPartyIdAndStatusOrderByJoinedAtAsc(partyId, PartyMember.Status.PENDING).stream()
                .map(PartyJoinRequestResponse::from)
                .toList();
    }

    public MyPartiesResponse getMyParties(Long userId) {
        List<PartySummaryResponse> owned = studyPartyRepository.findByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toSummary)
                .toList();

        List<PartySummaryResponse> joined = partyMemberRepository.findByUserIdAndStatus(userId, PartyMember.Status.APPROVED).stream()
                .map(PartyMember::getParty)
                .filter(party -> !party.isOwner(userId))
                .map(this::toSummary)
                .toList();

        List<PartySummaryResponse> pending = partyMemberRepository.findByUserIdAndStatus(userId, PartyMember.Status.PENDING).stream()
                .map(PartyMember::getParty)
                .map(this::toSummary)
                .toList();

        return new MyPartiesResponse(owned, joined, pending);
    }

    @Transactional
    public Long create(Long ownerId, PartyRequest request) {
        User owner = userService.getUserOrThrow(ownerId);
        validateProfileCompleted(owner);

        StudyParty party = studyPartyRepository.save(StudyParty.builder()
                .owner(owner)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .capacity(request.capacity())
                .openChatUrl(request.openChatUrl())
                .recruitmentType(request.recruitmentType())
                .build());

        partyMemberRepository.save(PartyMember.builder()
                .party(party)
                .user(owner)
                .status(PartyMember.Status.APPROVED)
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
        validateProfileCompleted(user);

        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.PARTY_CLOSED);
        }
        if (partyMemberRepository.existsByPartyIdAndUserId(partyId, userId)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        if (party.isFirstCome()) {
            long approvedCount = partyMemberRepository.countByPartyIdAndStatus(partyId, PartyMember.Status.APPROVED);
            if (approvedCount >= party.getCapacity()) {
                throw new BusinessException(ErrorCode.PARTY_FULL);
            }
            partyMemberRepository.save(PartyMember.builder().party(party).user(user).status(PartyMember.Status.APPROVED).build());
            syncStatus(party);
        } else {
            partyMemberRepository.save(PartyMember.builder().party(party).user(user).status(PartyMember.Status.PENDING).build());
        }
    }

    @Transactional
    public void approveJoinRequest(Long partyId, Long memberId, Long ownerId) {
        StudyParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        PartyMember member = getPendingMemberOrThrow(partyId, memberId);

        long approvedCount = partyMemberRepository.countByPartyIdAndStatus(partyId, PartyMember.Status.APPROVED);
        if (approvedCount >= party.getCapacity()) {
            throw new BusinessException(ErrorCode.PARTY_FULL);
        }

        member.approve();
        syncStatus(party);
    }

    @Transactional
    public void rejectJoinRequest(Long partyId, Long memberId, Long ownerId) {
        StudyParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        PartyMember member = getPendingMemberOrThrow(partyId, memberId);

        member.reject();
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
        syncStatus(party);
    }

    private void syncStatus(StudyParty party) {
        long approvedCount = partyMemberRepository.countByPartyIdAndStatus(party.getId(), PartyMember.Status.APPROVED);
        if (approvedCount >= party.getCapacity() && party.isRecruiting()) {
            party.close();
        } else if (approvedCount < party.getCapacity() && !party.isRecruiting()) {
            party.reopen();
        }
    }

    private PartySummaryResponse toSummary(StudyParty party) {
        long memberCount = partyMemberRepository.countByPartyIdAndStatus(party.getId(), PartyMember.Status.APPROVED);
        return PartySummaryResponse.of(party, memberCount);
    }

    private StudyParty getPartyOrThrow(Long partyId) {
        return studyPartyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));
    }

    private PartyMember getPendingMemberOrThrow(Long partyId, Long memberId) {
        PartyMember member = partyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
        if (!member.getParty().getId().equals(partyId) || member.getStatus() != PartyMember.Status.PENDING) {
            throw new BusinessException(ErrorCode.JOIN_REQUEST_NOT_FOUND);
        }
        return member;
    }

    private void validateProfileCompleted(User user) {
        if (!user.isProfileCompleted()) {
            throw new BusinessException(ErrorCode.PROFILE_INCOMPLETE);
        }
    }
}
