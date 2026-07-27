package com.moa.backend.party.dto;

import java.util.List;

public record MyPartiesResponse(
        List<PartySummaryResponse> owned,
        List<PartySummaryResponse> joined,
        List<PartySummaryResponse> pending
) {
}
