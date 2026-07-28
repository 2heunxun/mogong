package com.moa.backend.dinnerparty.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "로그인한 사용자의 저녁팟 현황을 세 가지 목록으로 묶은 응답")
public record MyDinnerPartiesResponse(
        @Schema(description = "내가 파티장인 저녁팟 목록") List<DinnerPartySummaryResponse> owned,
        @Schema(description = "참여 신청이 승인(APPROVED)되어 활동중인 저녁팟 목록 (내가 만든 저녁팟은 제외)") List<DinnerPartySummaryResponse> joined,
        @Schema(description = "참여 신청 후 파티장의 승인/거절을 기다리는 중인 저녁팟 목록") List<DinnerPartySummaryResponse> pending
) {
}
