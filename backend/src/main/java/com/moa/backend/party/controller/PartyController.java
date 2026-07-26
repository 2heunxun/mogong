package com.moa.backend.party.controller;

import com.moa.backend.global.dto.PageResponse;
import com.moa.backend.party.dto.PartyDetailResponse;
import com.moa.backend.party.dto.PartyMemberResponse;
import com.moa.backend.party.dto.PartyRequest;
import com.moa.backend.party.dto.PartySummaryResponse;
import com.moa.backend.party.entity.StudyParty;
import com.moa.backend.party.service.PartyService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @GetMapping
    public PageResponse<PartySummaryResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        StudyParty.Status statusFilter = parseStatus(status);
        return PageResponse.of(partyService.list(category, keyword, statusFilter, pageable));
    }

    @GetMapping("/{id}")
    public PartyDetailResponse detail(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return partyService.getDetail(id, userId);
    }

    @GetMapping("/{id}/members")
    public List<PartyMemberResponse> members(@PathVariable Long id) {
        return partyService.getMembers(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@RequestBody @Valid PartyRequest request, @AuthenticationPrincipal Long userId) {
        return Map.of("id", partyService.create(userId, request));
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody @Valid PartyRequest request, @AuthenticationPrincipal Long userId) {
        partyService.update(id, userId, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        partyService.delete(id, userId);
    }

    @PostMapping("/{id}/join")
    public void join(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        partyService.join(id, userId);
    }

    @DeleteMapping("/{id}/leave")
    public void leave(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        partyService.leave(id, userId);
    }

    private StudyParty.Status parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return StudyParty.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
