package com.moa.backend.weekendparty.service;

import com.moa.backend.global.exception.BusinessException;
import com.moa.backend.global.exception.ErrorCode;
import com.moa.backend.user.entity.User;
import com.moa.backend.user.service.UserService;
import com.moa.backend.weekendparty.dto.MyWeekendPartiesResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyDetailResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyJoinRequestResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyMemberResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyRequest;
import com.moa.backend.weekendparty.dto.WeekendPartySummaryResponse;
import com.moa.backend.weekendparty.entity.WeekendParty;
import com.moa.backend.weekendparty.entity.WeekendPartyMember;
import com.moa.backend.weekendparty.repository.WeekendPartyMemberRepository;
import com.moa.backend.weekendparty.repository.WeekendPartyRepository;
import com.moa.backend.weekendparty.repository.WeekendPartySpecs;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeekendPartyService {

    private final WeekendPartyRepository weekendPartyRepository;
    private final WeekendPartyMemberRepository weekendPartyMemberRepository;
    private final UserService userService;

    public Page<WeekendPartySummaryResponse> list(String category, String keyword, WeekendParty.Status status, Pageable pageable) {
        var spec = WeekendPartySpecs.withFilters(category, keyword, status);
        return weekendPartyRepository.findAll(spec, pageable)
                .map(this::toSummary);
    }

    public WeekendPartyDetailResponse getDetail(Long partyId, Long currentUserId) {
        WeekendParty party = getPartyOrThrow(partyId);
        long memberCount = weekendPartyMemberRepository.countByPartyIdAndStatus(partyId, WeekendPartyMember.Status.APPROVED);
        boolean isOwner = currentUserId != null && party.isOwner(currentUserId);
        WeekendPartyMember.Status memberStatus = currentUserId == null
                ? null
                : weekendPartyMemberRepository.findByPartyIdAndUserId(partyId, currentUserId).map(WeekendPartyMember::getStatus).orElse(null);
        return WeekendPartyDetailResponse.of(party, memberCount, isOwner, memberStatus);
    }

    public List<WeekendPartyMemberResponse> getMembers(Long partyId) {
        WeekendParty party = getPartyOrThrow(partyId);
        return weekendPartyMemberRepository.findByPartyIdAndStatusOrderByJoinedAtAsc(partyId, WeekendPartyMember.Status.APPROVED).stream()
                .map(member -> WeekendPartyMemberResponse.of(member, party.getOwner().getId()))
                .toList();
    }

    public List<WeekendPartyJoinRequestResponse> getJoinRequests(Long partyId, Long ownerId) {
        WeekendParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        return weekendPartyMemberRepository.findByPartyIdAndStatusOrderByJoinedAtAsc(partyId, WeekendPartyMember.Status.PENDING).stream()
                .map(WeekendPartyJoinRequestResponse::from)
                .toList();
    }

    public MyWeekendPartiesResponse getMyParties(Long userId) {
        List<WeekendPartySummaryResponse> owned = weekendPartyRepository.findByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toSummary)
                .toList();

        List<WeekendPartySummaryResponse> joined = weekendPartyMemberRepository.findByUserIdAndStatus(userId, WeekendPartyMember.Status.APPROVED).stream()
                .map(WeekendPartyMember::getParty)
                .filter(party -> !party.isOwner(userId))
                .map(this::toSummary)
                .toList();

        List<WeekendPartySummaryResponse> pending = weekendPartyMemberRepository.findByUserIdAndStatus(userId, WeekendPartyMember.Status.PENDING).stream()
                .map(WeekendPartyMember::getParty)
                .map(this::toSummary)
                .toList();

        return new MyWeekendPartiesResponse(owned, joined, pending);
    }

    @Transactional
    public Long create(Long ownerId, WeekendPartyRequest request) {
        User owner = userService.getUserOrThrow(ownerId);
        validateProfileCompleted(owner);

        WeekendParty party = weekendPartyRepository.save(WeekendParty.builder()
                .owner(owner)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .meetingAt(request.meetingAt())
                .capacity(request.capacity())
                .openChatUrl(request.openChatUrl())
                .recruitmentType(request.recruitmentType())
                .build());

        weekendPartyMemberRepository.save(WeekendPartyMember.builder()
                .party(party)
                .user(owner)
                .status(WeekendPartyMember.Status.APPROVED)
                .build());

        return party.getId();
    }

    @Transactional
    public void update(Long partyId, Long userId, WeekendPartyRequest request) {
        WeekendParty party = getPartyOrThrow(partyId);
        party.validateOwner(userId);
        party.update(request.title(), request.description(), request.category(), request.meetingAt(),
                request.capacity(), request.openChatUrl());
    }

    @Transactional
    public void delete(Long partyId, Long userId) {
        WeekendParty party = getPartyOrThrow(partyId);
        party.validateOwner(userId);
        weekendPartyRepository.delete(party);
    }

    @Transactional
    public void join(Long partyId, Long userId) {
        WeekendParty party = getPartyOrThrow(partyId);
        User user = userService.getUserOrThrow(userId);
        validateProfileCompleted(user);

        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.WEEKEND_PARTY_CLOSED);
        }
        if (weekendPartyMemberRepository.existsByPartyIdAndUserId(partyId, userId)) {
            throw new BusinessException(ErrorCode.WEEKEND_ALREADY_JOINED);
        }

        if (party.isFirstCome()) {
            long approvedCount = weekendPartyMemberRepository.countByPartyIdAndStatus(partyId, WeekendPartyMember.Status.APPROVED);
            if (approvedCount >= party.getCapacity()) {
                throw new BusinessException(ErrorCode.WEEKEND_PARTY_FULL);
            }
            weekendPartyMemberRepository.save(WeekendPartyMember.builder().party(party).user(user).status(WeekendPartyMember.Status.APPROVED).build());
            syncStatus(party);
        } else {
            weekendPartyMemberRepository.save(WeekendPartyMember.builder().party(party).user(user).status(WeekendPartyMember.Status.PENDING).build());
        }
    }

    @Transactional
    public void approveJoinRequest(Long partyId, Long memberId, Long ownerId) {
        WeekendParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        WeekendPartyMember member = getPendingMemberOrThrow(partyId, memberId);

        long approvedCount = weekendPartyMemberRepository.countByPartyIdAndStatus(partyId, WeekendPartyMember.Status.APPROVED);
        if (approvedCount >= party.getCapacity()) {
            throw new BusinessException(ErrorCode.WEEKEND_PARTY_FULL);
        }

        member.approve();
        syncStatus(party);
    }

    @Transactional
    public void rejectJoinRequest(Long partyId, Long memberId, Long ownerId) {
        WeekendParty party = getPartyOrThrow(partyId);
        party.validateOwner(ownerId);
        WeekendPartyMember member = getPendingMemberOrThrow(partyId, memberId);

        member.reject();
    }

    @Transactional
    public void leave(Long partyId, Long userId) {
        WeekendParty party = getPartyOrThrow(partyId);

        if (party.isOwner(userId)) {
            throw new BusinessException(ErrorCode.WEEKEND_OWNER_CANNOT_LEAVE);
        }
        if (!weekendPartyMemberRepository.existsByPartyIdAndUserId(partyId, userId)) {
            throw new BusinessException(ErrorCode.WEEKEND_NOT_JOINED);
        }

        weekendPartyMemberRepository.deleteByPartyIdAndUserId(partyId, userId);
        syncStatus(party);
    }

    private void syncStatus(WeekendParty party) {
        long approvedCount = weekendPartyMemberRepository.countByPartyIdAndStatus(party.getId(), WeekendPartyMember.Status.APPROVED);
        if (approvedCount >= party.getCapacity() && party.isRecruiting()) {
            party.close();
        } else if (approvedCount < party.getCapacity() && !party.isRecruiting()) {
            party.reopen();
        }
    }

    private WeekendPartySummaryResponse toSummary(WeekendParty party) {
        long memberCount = weekendPartyMemberRepository.countByPartyIdAndStatus(party.getId(), WeekendPartyMember.Status.APPROVED);
        return WeekendPartySummaryResponse.of(party, memberCount);
    }

    private WeekendParty getPartyOrThrow(Long partyId) {
        return weekendPartyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEKEND_PARTY_NOT_FOUND));
    }

    private WeekendPartyMember getPendingMemberOrThrow(Long partyId, Long memberId) {
        WeekendPartyMember member = weekendPartyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEKEND_JOIN_REQUEST_NOT_FOUND));
        if (!member.getParty().getId().equals(partyId) || member.getStatus() != WeekendPartyMember.Status.PENDING) {
            throw new BusinessException(ErrorCode.WEEKEND_JOIN_REQUEST_NOT_FOUND);
        }
        return member;
    }

    private void validateProfileCompleted(User user) {
        if (!user.isProfileCompleted()) {
            throw new BusinessException(ErrorCode.PROFILE_INCOMPLETE);
        }
    }
}
