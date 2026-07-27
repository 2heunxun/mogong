package com.moa.backend.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OnboardingRequest(
        @NotNull(message = "반은 필수입니다.")
        @Min(value = 1, message = "반은 1반 이상이어야 합니다.")
        @Max(value = 10, message = "반은 10반 이하여야 합니다.")
        Integer classNo,

        @NotNull(message = "조는 필수입니다.")
        @Min(value = 1, message = "조는 1조 이상이어야 합니다.")
        @Max(value = 30, message = "조는 30조 이하여야 합니다.")
        Integer teamNo,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
        String realName
) {
}
