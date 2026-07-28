package com.moa.backend.weekendparty.controller;

import com.moa.backend.global.dto.PageResponse;
import com.moa.backend.global.exception.ErrorResponse;
import com.moa.backend.weekendparty.dto.MyWeekendPartiesResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyDetailResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyJoinRequestResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyMemberResponse;
import com.moa.backend.weekendparty.dto.WeekendPartyRequest;
import com.moa.backend.weekendparty.dto.WeekendPartySummaryResponse;
import com.moa.backend.weekendparty.entity.WeekendParty;
import com.moa.backend.weekendparty.service.WeekendPartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "주말팟(WeekendParty)",
        description = """
                주말에 모여서 공부하는 사람을 구하는 주말팟 CRUD, 참여 신청/수락/거절, 내 주말팟 목록을 담당한다.
                스터디 파티(Party) API와 동일한 규칙을 따른다.
                """
)
@RestController
@RequestMapping("/api/weekend-parties")
@RequiredArgsConstructor
public class WeekendPartyController {

    private final WeekendPartyService weekendPartyService;

    @Operation(summary = "주말팟 목록 조회 (검색/필터/페이지네이션)",
            description = "카테고리, 키워드(제목/소개 부분 일치), 모집 상태(`RECRUITING`/`CLOSED`)로 필터링한 주말팟 목록을 페이지 단위로 반환한다. 로그인 불필요.")
    @SecurityRequirements
    @GetMapping
    public PageResponse<WeekendPartySummaryResponse> list(
            @Parameter(description = "카테고리 필터 (예: 코딩테스트, 어학, 자격증, 취업, 전공, 기타). 비우면 전체.")
            @RequestParam(required = false) String category,
            @Parameter(description = "제목/소개에 대한 부분 일치 검색어. 비우면 전체.")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "모집 상태 필터: RECRUITING(모집중) 또는 CLOSED(마감). 값이 없거나 잘못되면 전체 조회.")
            @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호(0부터), 크기, 정렬. 기본 size=10, createdAt 기준 정렬.")
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        WeekendParty.Status statusFilter = parseStatus(status);
        return PageResponse.of(weekendPartyService.list(category, keyword, statusFilter, pageable));
    }

    @Operation(summary = "내 주말팟 목록 (만든 주말팟 / 참여 중 / 신청 중)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰이 없거나 유효하지 않음 (본문 없음)", content = @Content)
    })
    @GetMapping("/mine")
    public MyWeekendPartiesResponse mine(@AuthenticationPrincipal Long userId) {
        return weekendPartyService.getMyParties(userId);
    }

    @Operation(summary = "주말팟 상세 조회")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟 (`WEEKEND_PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public WeekendPartyDetailResponse detail(
            @Parameter(description = "주말팟 ID") @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return weekendPartyService.getDetail(id, userId);
    }

    @Operation(summary = "주말팟 파티원 목록 조회", description = "승인(APPROVED)된 파티원만 반환한다. 로그인 불필요.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟 (`WEEKEND_PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/members")
    public List<WeekendPartyMemberResponse> members(@Parameter(description = "주말팟 ID") @PathVariable Long id) {
        return weekendPartyService.getMembers(id);
    }

    @Operation(summary = "주말팟 생성",
            description = "새 주말팟을 만든다. 생성자는 자동으로 파티장이자 승인(APPROVED) 상태의 파티원으로 등록된다. 가입(온보딩)을 완료하지 않은 사용자는 생성할 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공, 생성된 주말팟의 id 반환"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "회원가입(온보딩) 미완료 (`PROFILE_INCOMPLETE`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@RequestBody @Valid WeekendPartyRequest request, @AuthenticationPrincipal Long userId) {
        return Map.of("id", weekendPartyService.create(userId, request));
    }

    @Operation(summary = "주말팟 정보 수정", description = "파티장만 수정할 수 있다. 전체 필드를 교체한다(PUT).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_WEEKEND_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟 (`WEEKEND_PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public void update(
            @Parameter(description = "주말팟 ID") @PathVariable Long id,
            @RequestBody @Valid WeekendPartyRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        weekendPartyService.update(id, userId, request);
    }

    @Operation(summary = "주말팟 삭제", description = "파티장만 삭제할 수 있다. 파티원/참여 신청 기록도 함께 삭제된다(cascade).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_WEEKEND_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟 (`WEEKEND_PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public void delete(@Parameter(description = "주말팟 ID") @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        weekendPartyService.delete(id, userId);
    }

    @Operation(summary = "주말팟 참여 신청",
            description = "참여 신청(PENDING)을 생성한다. 정원 검사는 파티장이 수락하는 시점에만 수행된다. 모집 마감된 주말팟은 신청할 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 성공 (상태: PENDING)"),
            @ApiResponse(responseCode = "403", description = "회원가입(온보딩) 미완료 (`PROFILE_INCOMPLETE`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟 (`WEEKEND_PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "모집 마감(`WEEKEND_PARTY_CLOSED`) 또는 이미 신청/참여/거절 기록 있음(`WEEKEND_ALREADY_JOINED`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/join")
    public void join(@Parameter(description = "주말팟 ID") @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        weekendPartyService.join(id, userId);
    }

    @Operation(summary = "주말팟 탈퇴 / 참여 신청 취소",
            description = "내 참여 기록(PENDING/APPROVED/REJECTED 무관)을 삭제한다. 파티장 본인은 탈퇴할 수 없다(주말팟을 삭제해야 함).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟 (`WEEKEND_PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "파티장은 탈퇴 불가(`WEEKEND_OWNER_CANNOT_LEAVE`) 또는 참여/신청 기록 없음(`WEEKEND_NOT_JOINED`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}/leave")
    public void leave(@Parameter(description = "주말팟 ID") @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        weekendPartyService.leave(id, userId);
    }

    @Operation(summary = "참여 신청 대기 목록 조회 (파티장 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_WEEKEND_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟 (`WEEKEND_PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/join-requests")
    public List<WeekendPartyJoinRequestResponse> joinRequests(
            @Parameter(description = "주말팟 ID") @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return weekendPartyService.getJoinRequests(id, userId);
    }

    @Operation(summary = "참여 신청 수락 (파티장 전용)",
            description = "승인 시점에 현재 승인된 파티원 수가 정원(capacity) 이상이면 거부된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수락 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_WEEKEND_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟이거나, 존재하지 않는/이미 처리된 신청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "정원이 가득 찬 주말팟 (`WEEKEND_PARTY_FULL`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/join-requests/{memberId}/approve")
    public void approveJoinRequest(
            @Parameter(description = "주말팟 ID") @PathVariable Long id,
            @Parameter(description = "승인할 참여 신청의 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        weekendPartyService.approveJoinRequest(id, memberId, userId);
    }

    @Operation(summary = "참여 신청 거절 (파티장 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_WEEKEND_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주말팟이거나, 존재하지 않는/이미 처리된 신청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/join-requests/{memberId}/reject")
    public void rejectJoinRequest(
            @Parameter(description = "주말팟 ID") @PathVariable Long id,
            @Parameter(description = "거절할 참여 신청의 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        weekendPartyService.rejectJoinRequest(id, memberId, userId);
    }

    private WeekendParty.Status parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return WeekendParty.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
