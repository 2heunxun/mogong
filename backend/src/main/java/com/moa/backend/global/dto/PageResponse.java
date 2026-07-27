package com.moa.backend.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "Spring Data `Page`를 감싼 공통 페이지네이션 응답 형식")
public record PageResponse<T>(
        @Schema(description = "현재 페이지의 데이터 목록") List<T> content,
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,
        @Schema(description = "페이지당 항목 수", example = "10") int size,
        @Schema(description = "전체 항목 수") long totalElements,
        @Schema(description = "전체 페이지 수") int totalPages,
        @Schema(description = "마지막 페이지 여부") boolean last
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
