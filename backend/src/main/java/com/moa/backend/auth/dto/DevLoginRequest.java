package com.moa.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로컬 개발용 로그인 요청. 동일한 닉네임으로 다시 호출하면 같은 계정으로 로그인된다.")
public record DevLoginRequest(
        @Schema(description = "가상 계정을 식별할 닉네임", example = "테스트유저")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {
}
