package com.moa.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "카카오 로그인 콜백에서 받은 인가 코드로 로그인을 완료하기 위한 요청")
public record KakaoLoginRequest(
        @Schema(description = "카카오가 `KAKAO_REDIRECT_URI`로 리다이렉트하며 실어준 `code` 쿼리 파라미터 값. 1회용이며 유효 시간이 짧다.")
        @NotBlank(message = "인가 코드는 필수입니다.") String code
) {
}
