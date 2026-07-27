package com.moa.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 완료(온보딩) 요청. 세 값 모두 필수.")
public record OnboardingRequest(
        @Schema(description = "소속 반 번호 (1~10)", example = "3", minimum = "1", maximum = "10")
        @NotNull(message = "반은 필수입니다.")
        @Min(value = 1, message = "반은 1반 이상이어야 합니다.")
        @Max(value = 10, message = "반은 10반 이하여야 합니다.")
        Integer classNo,

        @Schema(description = "소속 조 번호 (1~30)", example = "2", minimum = "1", maximum = "30")
        @NotNull(message = "조는 필수입니다.")
        @Min(value = 1, message = "조는 1조 이상이어야 합니다.")
        @Max(value = 30, message = "조는 30조 이하여야 합니다.")
        Integer teamNo,

        @Schema(description = "실명 (최대 20자). 카카오 닉네임이 아닌 실제 이름을 입력한다.", example = "홍길동", maxLength = 20)
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
        String realName
) {
}
