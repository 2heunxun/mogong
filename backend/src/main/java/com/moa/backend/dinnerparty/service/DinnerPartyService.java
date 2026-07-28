package com.moa.backend.dinnerparty.service;

import com.moa.backend.dinnerparty.dto.DinnerPartyDetailResponse;
import com.moa.backend.dinnerparty.dto.DinnerPartyJoinRequestResponse;
import com.moa.backend.dinnerparty.dto.DinnerPartyMemberResponse;
import com.moa.backend.dinnerparty.dto.DinnerPartyRequest;
import com.moa.backend.dinnerparty.dto.DinnerPartySummaryResponse;
import com.moa.backend.dinnerparty.dto.MyDinnerPartiesResponse;
import com.moa.backend.dinnerparty.entity.DinnerParty;
import com.moa.backend.dinnerparty.entity.DinnerPartyMember;
import com.moa.backend.dinnerparty.repository.DinnerPartyMemberRepository;
import com.moa.backend.dinnerparty.repository.DinnerPartyRepository;
import com.moa.backend.dinnerparty.repository.DinnerPartySpecs;
import com.moa.backend.global.exception.BusinessException;
import com.moa.backend.global.exception.ErrorCode;
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
public class DinnerPartyService {

    private final DinnerPartyRepository dinnerPartyRepository;
    private final DinnerPartyMemberRepository dinnerPartyMemberRepository;
    private final UserService userService;

    public Page<DinnerPartySummaryResponse> list(String keyword, DinnerParty.Status status, Pageable pageable) {
        var spec = DinnerPartySpecs.withFilters(keyword, status);
        return dinnerPartyRepository.findAll(spec, pageable)
                .map(this::toSummary);
    }

    public DinnerPartyDetailResponse getDetail(Long partyId, Long currentUserId) {
        DinnerParty party = getPartyOrThrow(partyId);
        long memberCount = dinnerPartyMemberRepository.countByPartyIdAndStatus(partyId, DinnerPartyMember.Status.APPROVED);
        boolean isOwner = currentUserId != null && party.isOwner(currentUserId);
        DinnerPartyMember.Status memberStatus = currentUserId == null
                ? null
                : dinnerPartyMemberRepository.findByPartyIdAndUserId(partyId, currentUserId).map(DinnerPartyMember::getStatus).orElse(null);
        return DinnerPartyDetailResponse.of(party, memberCount, isOwner, memberStatus);
    }

    public List<DinnerPartyMemberResponse> getMembers(Long partyId) {
        DinnerParty party = getPartyOrThrow(partyId);
        return dinnerPartyMemberRepository.findByPartyIdAndStatusOrderByJoinedAtAsc(partyId, DinnerPartyMember.Status.APPROVED).stream()
                .map(member -> DinnerPartyMemberResponse.of(member, party.getOwner().getId()))
                .toList();
    }

    public List<DinnerPartyJoinRequestResponse> getJoinRequests(Long partyId, Long ownerId) {
        DinnerParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        return dinnerPartyMemberRepository.findByPartyIdAndStatusOrderByJoinedAtAsc(partyId, DinnerPartyMember.Status.PENDING).stream()
                .map(DinnerPartyJoinRequestResponse::from)
                .toList();
    }

    public MyDinnerPartiesResponse getMyParties(Long userId) {
        List<DinnerPartySummaryResponse> owned = dinnerPartyRepository.findByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toSummary)
                .toList();

        List<DinnerPartySummaryResponse> joined = dinnerPartyMemberRepository.findByUserIdAndStatus(userId, DinnerPartyMember.Status.APPROVED).stream()
                .map(DinnerPartyMember::getParty)
                .filter(party -> !party.isOwner(userId))
                .map(this::toSummary)
                .toList();

        List<DinnerPartySummaryResponse> pending = dinnerPartyMemberRepository.findByUserIdAndStatus(userId, DinnerPartyMember.Status.PENDING).stream()
                .map(DinnerPartyMember::getParty)
                .map(this::toSummary)
                .toList();

        return new MyDinnerPartiesResponse(owned, joined, pending);
    }

    @Transactional
    public Long create(Long ownerId, DinnerPartyRequest request) {
        User owner = userService.getUserOrThrow(ownerId);
        validateProfileCompleted(owner);

        DinnerParty party = dinnerPartyRepository.save(DinnerParty.builder()
                .owner(owner)
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .meetingAt(request.meetingAt())
                .capacity(request.capacity())
                .openChatUrl(request.openChatUrl())
                .build());

        dinnerPartyMemberRepository.save(DinnerPartyMember.builder()
                .party(party)
                .user(owner)
                .status(DinnerPartyMember.Status.APPROVED)
                .build());

        return party.getId();
    }

    @Transactional
    public void update(Long partyId, Long userId, DinnerPartyRequest request) {
        DinnerParty party = getPartyOrThrow(partyId);
        party.validateOwner(userId);
        party.update(request.title(), request.description(), request.location(), request.meetingAt(),
                request.capacity(), request.openChatUrl());
    }

    @Transactional
    public void delete(Long partyId, Long userId) {
        DinnerParty party = getPartyOrThrow(partyId);
        party.validateOwner(userId);
        dinnerPartyRepository.delete(party);
    }

    @Transactional
    public void join(Long partyId, Long userId) {
        DinnerParty party = getPartyOrThrow(partyId);
        User user = userService.getUserOrThrow(userId);
        validateProfileCompleted(user);

        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.DINNER_PARTY_CLOSED);
        }
        if (dinnerPartyMemberRepository.existsByPartyIdAndUserId(partyId, userId)) {
            throw new BusinessException(ErrorCode.DINNER_ALREADY_JOINED);
        }

        dinnerPartyMemberRepository.save(DinnerPartyMember.builder().party(party).user(user).status(DinnerPartyMember.Status.PENDING).build());
    }

    @Transactional
    public void approveJoinRequest(Long partyId, Long memberId, Long ownerId) {
        DinnerParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        DinnerPartyMember member = getPendingMemberOrThrow(partyId, memberId);

        long approvedCount = dinnerPartyMemberRepository.countByPartyIdAndStatus(partyId, DinnerPartyMember.Status.APPROVED);
        if (approvedCount >= party.getCapacity()) {
            throw new BusinessException(ErrorCode.DINNER_PARTY_FULL);
        }

        member.approve();
    }

    @Transactional
    public void rejectJoinRequest(Long partyId, Long memberId, Long ownerId) {
        DinnerParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        DinnerPartyMember member = getPendingMemberOrThrow(partyId, memberId);

        member.reject();
    }

    @Transactional
    public void leave(Long partyId, Long userId) {
        DinnerParty party = getPartyOrThrow(partyId);

        if (party.isOwner(userId)) {
            throw new BusinessException(ErrorCode.DINNER_OWNER_CANNOT_LEAVE);
        }
        if (!dinnerPartyMemberRepository.existsByPartyIdAndUserId(partyId, userId)) {
            throw new BusinessException(ErrorCode.DINNER_NOT_JOINED);
        }

        dinnerPartyMemberRepository.deleteByPartyIdAndUserId(partyId, userId);
    }

    private DinnerPartySummaryResponse toSummary(DinnerParty party) {
        long memberCount = dinnerPartyMemberRepository.countByPartyIdAndStatus(party.getId(), DinnerPartyMember.Status.APPROVED);
        return DinnerPartySummaryResponse.of(party, memberCount);
    }

    private DinnerParty getPartyOrThrow(Long partyId) {
        return dinnerPartyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DINNER_PARTY_NOT_FOUND));
    }

    private DinnerPartyMember getPendingMemberOrThrow(Long partyId, Long memberId) {
        DinnerPartyMember member = dinnerPartyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DINNER_JOIN_REQUEST_NOT_FOUND));
        if (!member.getParty().getId().equals(partyId) || member.getStatus() != DinnerPartyMember.Status.PENDING) {
            throw new BusinessException(ErrorCode.DINNER_JOIN_REQUEST_NOT_FOUND);
        }
        return member;
    }

    private void validateProfileCompleted(User user) {
        if (!user.isProfileCompleted()) {
            throw new BusinessException(ErrorCode.PROFILE_INCOMPLETE);
        }
    }
}
