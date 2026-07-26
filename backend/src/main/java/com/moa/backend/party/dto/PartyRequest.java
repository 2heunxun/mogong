package com.moa.backend.party.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PartyRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Size(max = 2000, message = "소개는 2000자 이하여야 합니다.")
        String description,

        @NotBlank(message = "카테고리는 필수입니다.")
        @Size(max = 30, message = "카테고리는 30자 이하여야 합니다.")
        String category,

        @NotNull(message = "정원은 필수입니다.")
        @Min(value = 2, message = "정원은 2명 이상이어야 합니다.")
        @Max(value = 100, message = "정원은 100명 이하여야 합니다.")
        Integer capacity,

        @NotBlank(message = "카카오 오픈채팅 링크는 필수입니다.")
        @Pattern(regexp = "^https://open\\.kakao\\.com/.+$", message = "open.kakao.com 형식의 오픈채팅 링크만 등록할 수 있습니다.")
        String openChatUrl
) {
}
