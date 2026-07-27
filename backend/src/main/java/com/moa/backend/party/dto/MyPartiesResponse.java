package com.moa.backend.party.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "로그인한 사용자의 파티 현황을 세 가지 목록으로 묶은 응답")
public record MyPartiesResponse(
        @Schema(description = "내가 파티장인 파티 목록") List<PartySummaryResponse> owned,
        @Schema(description = "참여 신청이 승인(APPROVED)되어 활동중인 파티 목록 (내가 만든 파티는 제외)") List<PartySummaryResponse> joined,
        @Schema(description = "참여 신청 후 파티장의 승인/거절을 기다리는 중인 파티 목록") List<PartySummaryResponse> pending
) {
}
