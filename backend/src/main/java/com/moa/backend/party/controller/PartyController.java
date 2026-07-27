package com.moa.backend.party.controller;

import com.moa.backend.global.dto.PageResponse;
import com.moa.backend.global.exception.ErrorResponse;
import com.moa.backend.party.dto.MyPartiesResponse;
import com.moa.backend.party.dto.PartyDetailResponse;
import com.moa.backend.party.dto.PartyJoinRequestResponse;
import com.moa.backend.party.dto.PartyMemberResponse;
import com.moa.backend.party.dto.PartyRequest;
import com.moa.backend.party.dto.PartySummaryResponse;
import com.moa.backend.party.entity.StudyParty;
import com.moa.backend.party.service.PartyService;
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
        name = "스터디 파티(Party)",
        description = """
                스터디 파티 CRUD, 참여 신청/수락/거절, 내 파티 목록을 담당한다.

                파티 목록/상세/파티원 조회(GET)는 로그인 없이도 볼 수 있지만, 로그인 토큰을 함께 보내면
                상세 응답에 "내 참여 상태(memberStatus)"와 "내가 파티장인지(isOwner)"가 채워지고,
                오픈채팅 링크도 조건에 맞을 때만 노출된다. 그 외 생성/수정/삭제/참여/신청관리는 모두
                로그인이 필요하다.
                """
)
@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @Operation(
            summary = "파티 목록 조회 (검색/필터/페이지네이션)",
            description = """
                    카테고리, 키워드(제목/소개 부분 일치), 모집 상태(`RECRUITING`/`CLOSED`)로 필터링한
                    파티 목록을 페이지 단위로 반환한다. `memberCount`는 승인(APPROVED)된 파티원 수만
                    집계한 값이다(대기중인 신청자는 포함하지 않음).

                    로그인 불필요.
                    """
    )
    @SecurityRequirements
    @GetMapping
    public PageResponse<PartySummaryResponse> list(
            @Parameter(description = "카테고리 필터 (예: 코딩테스트, 어학, 자격증, 취업, 전공, 기타). 비우면 전체.")
            @RequestParam(required = false) String category,
            @Parameter(description = "제목/소개에 대한 부분 일치 검색어. 비우면 전체.")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "모집 상태 필터: RECRUITING(모집중) 또는 CLOSED(마감). 값이 없거나 잘못되면 전체 조회.")
            @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호(0부터), 크기, 정렬. 기본 size=10, createdAt 기준 정렬.")
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        StudyParty.Status statusFilter = parseStatus(status);
        return PageResponse.of(partyService.list(category, keyword, statusFilter, pageable));
    }

    @Operation(
            summary = "내 파티 목록 (만든 파티 / 참여 중 / 신청 중)",
            description = """
                    로그인한 사용자를 기준으로 세 가지 목록을 한 번에 반환한다.
                    - `owned`: 내가 파티장인 파티
                    - `joined`: 내가 참여 신청이 승인(APPROVED)된 파티 (내가 만든 파티는 제외)
                    - `pending`: 내가 참여 신청했지만 아직 파티장의 승인/거절을 기다리는 중인 파티
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰이 없거나 유효하지 않음 (본문 없음)", content = @Content)
    })
    @GetMapping("/mine")
    public MyPartiesResponse mine(@AuthenticationPrincipal Long userId) {
        return partyService.getMyParties(userId);
    }

    @Operation(
            summary = "파티 상세 조회",
            description = """
                    로그인 없이도 조회할 수 있지만, `Authorization` 헤더를 포함해서 요청하면 응답의
                    `isOwner`(내가 파티장인지)와 `memberStatus`(내 참여 상태: null=신청 안 함,
                    `PENDING`=승인 대기, `APPROVED`=참여중, `REJECTED`=거절됨)가 로그인 사용자 기준으로
                    채워진다.

                    `openChatUrl`(카카오 오픈채팅 링크)은 파티장 본인이거나 `memberStatus = APPROVED`인
                    경우에만 값이 채워지고, 그 외에는 `null`로 응답해 비공개 링크가 노출되지 않도록 한다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티 (`PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public PartyDetailResponse detail(
            @Parameter(description = "파티 ID") @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return partyService.getDetail(id, userId);
    }

    @Operation(
            summary = "파티원 목록 조회",
            description = "승인(APPROVED)된 파티원만 반환한다. 참여 신청 대기/거절 상태는 포함되지 않는다. 로그인 불필요."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티 (`PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/members")
    public List<PartyMemberResponse> members(@Parameter(description = "파티 ID") @PathVariable Long id) {
        return partyService.getMembers(id);
    }

    @Operation(
            summary = "파티 생성",
            description = """
                    새 스터디 파티를 만든다. 생성자는 자동으로 파티장이자 승인(APPROVED) 상태의 파티원으로
                    등록된다.

                    가입(온보딩)을 완료하지 않은 사용자는 생성할 수 없다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공, 생성된 파티의 id 반환"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 (제목/카테고리/오픈채팅 링크 형식 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "회원가입(온보딩) 미완료 (`PROFILE_INCOMPLETE`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@RequestBody @Valid PartyRequest request, @AuthenticationPrincipal Long userId) {
        return Map.of("id", partyService.create(userId, request));
    }

    @Operation(
            summary = "파티 정보 수정",
            description = "파티장만 수정할 수 있다. 제목/소개/카테고리/정원/오픈채팅 링크를 전체 교체한다(PUT)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티 (`PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public void update(
            @Parameter(description = "파티 ID") @PathVariable Long id,
            @RequestBody @Valid PartyRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        partyService.update(id, userId, request);
    }

    @Operation(
            summary = "파티 삭제",
            description = "파티장만 삭제할 수 있다. 파티원/참여 신청 기록도 함께 삭제된다(cascade)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티 (`PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public void delete(@Parameter(description = "파티 ID") @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        partyService.delete(id, userId);
    }

    @Operation(
            summary = "파티 참여 신청",
            description = """
                    파티 참여는 즉시 멤버가 되는 것이 아니라 **참여 신청(PENDING)** 을 생성하는 것이다.
                    파티장이 이후 수락(approve)해야 실제 파티원(APPROVED)이 된다.

                    정원이 이미 찼더라도 신청 자체는 막지 않는다 (정원 검사는 파티장이 "수락"하는
                    시점에만 수행된다). 모집 마감(`CLOSED`) 상태의 파티는 신청할 수 없다.
                    이미 신청/참여/거절 기록이 남아있는 파티에는 다시 신청할 수 없으며, 먼저
                    `DELETE /{id}/leave`로 기존 기록을 지워야 재신청할 수 있다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 성공 (상태: PENDING)"),
            @ApiResponse(responseCode = "403", description = "회원가입(온보딩) 미완료 (`PROFILE_INCOMPLETE`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티 (`PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "모집 마감된 파티 (`PARTY_CLOSED`) 또는 이미 신청/참여/거절 기록이 있음 (`ALREADY_JOINED`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/join")
    public void join(@Parameter(description = "파티 ID") @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        partyService.join(id, userId);
    }

    @Operation(
            summary = "파티 탈퇴 / 참여 신청 취소",
            description = """
                    내 참여 기록(PENDING/APPROVED/REJECTED 무관)을 삭제한다. 용도에 따라 의미가 달라진다.
                    - `PENDING` 상태에서 호출 → 참여 신청 취소
                    - `APPROVED` 상태에서 호출 → 파티 탈퇴
                    - `REJECTED` 상태에서 호출 → 거절 기록을 지우고 재신청 가능한 상태로 초기화

                    파티장 본인은 탈퇴할 수 없다(파티를 삭제해야 함).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티 (`PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "파티장은 탈퇴 불가 (`OWNER_CANNOT_LEAVE`) 또는 참여/신청 기록이 없음 (`NOT_JOINED`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}/leave")
    public void leave(@Parameter(description = "파티 ID") @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        partyService.leave(id, userId);
    }

    @Operation(
            summary = "참여 신청 대기 목록 조회 (파티장 전용)",
            description = "현재 파티에 대해 승인/거절되지 않고 대기(PENDING) 중인 참여 신청 목록을 신청한 순서대로 반환한다. 파티장만 조회할 수 있다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티 (`PARTY_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/join-requests")
    public List<PartyJoinRequestResponse> joinRequests(
            @Parameter(description = "파티 ID") @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return partyService.getJoinRequests(id, userId);
    }

    @Operation(
            summary = "참여 신청 수락 (파티장 전용)",
            description = """
                    대기중(PENDING)인 신청을 승인(APPROVED)한다. 승인 시점에 현재 승인된 파티원 수가
                    정원(capacity) 이상이면 거부된다 — 즉 **정원 초과 검사는 신청 시점이 아니라 이
                    수락 시점에 이루어진다.**
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수락 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티이거나, 존재하지 않는/이미 처리된 신청 (`PARTY_NOT_FOUND`, `JOIN_REQUEST_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "정원이 가득 찬 파티 (`PARTY_FULL`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/join-requests/{memberId}/approve")
    public void approveJoinRequest(
            @Parameter(description = "파티 ID") @PathVariable Long id,
            @Parameter(description = "승인할 참여 신청의 ID (`GET /{id}/join-requests` 응답의 `id` 값)") @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        partyService.approveJoinRequest(id, memberId, userId);
    }

    @Operation(
            summary = "참여 신청 거절 (파티장 전용)",
            description = "대기중(PENDING)인 신청을 거절(REJECTED) 상태로 바꾼다. 신청자는 이후 `DELETE /{id}/leave`로 거절 기록을 지우고 재신청할 수 있다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "403", description = "파티장이 아님 (`NOT_PARTY_OWNER`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 파티이거나, 존재하지 않는/이미 처리된 신청 (`PARTY_NOT_FOUND`, `JOIN_REQUEST_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/join-requests/{memberId}/reject")
    public void rejectJoinRequest(
            @Parameter(description = "파티 ID") @PathVariable Long id,
            @Parameter(description = "거절할 참여 신청의 ID (`GET /{id}/join-requests` 응답의 `id` 값)") @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        partyService.rejectJoinRequest(id, memberId, userId);
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
