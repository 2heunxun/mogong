package com.moa.backend.user.controller;

import com.moa.backend.global.exception.ErrorResponse;
import com.moa.backend.user.dto.OnboardingRequest;
import com.moa.backend.user.dto.UserResponse;
import com.moa.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "사용자(User)",
        description = "내 정보 조회 및 회원가입 마무리(온보딩)를 담당한다. 전 endpoint 로그인(Bearer 토큰) 필요."
)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 반환한다. `profileCompleted`로 온보딩(가입) 완료 여부를 판단할 수 있다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰이 없거나 유효하지 않음 (본문 없음)", content = @Content)
    })
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        return userService.getMe(userId);
    }

    @Operation(
            summary = "회원가입 완료(온보딩) - 반/조/이름 등록",
            description = """
                    카카오(또는 개발용) 로그인이 성공했더라도, 반/조/실명을 등록해야 실제 회원가입이
                    완료된다(`profileCompleted = true`). 완료 전까지는 파티 생성/참여 신청이 모두
                    `403 PROFILE_INCOMPLETE`로 거부된다.

                    성공 시 닉네임이 `N반_N조_실명` 형식(예: `3반_2조_홍길동`)으로 자동 설정되며, 이 값이
                    파티 목록/파티원 목록 등 서비스 전반의 표시 닉네임으로 사용된다. 이후 카카오로 재로그인해도
                    이 닉네임은 카카오 닉네임으로 덮어써지지 않는다.

                    이미 온보딩을 완료한 사용자가 다시 호출하면 값을 덮어써 갱신한다(재수정 가능).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 완료 처리 성공, 갱신된 사용자 정보 반환"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 (반 1~10 범위, 조 1~30 범위, 실명 필수/20자 이하)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 토큰이 없거나 유효하지 않음 (본문 없음)", content = @Content)
    })
    @PatchMapping("/me/onboarding")
    public UserResponse completeOnboarding(@AuthenticationPrincipal Long userId, @RequestBody @Valid OnboardingRequest request) {
        return userService.completeOnboarding(userId, request);
    }
}
