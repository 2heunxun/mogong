package com.moa.backend.auth.controller;

import com.moa.backend.auth.dto.DevLoginRequest;
import com.moa.backend.auth.dto.KakaoLoginRequest;
import com.moa.backend.auth.dto.RefreshRequest;
import com.moa.backend.auth.dto.TokenResponse;
import com.moa.backend.auth.service.AuthService;
import com.moa.backend.global.config.KakaoProperties;
import com.moa.backend.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "인증(Auth)",
        description = """
                카카오 로그인 / 개발용 로그인 / 토큰 재발급 / 로그아웃을 담당한다.
                이 태그의 엔드포인트는 로그인 자체를 처리하는 곳이라 (로그아웃 제외) 인증이 필요 없다.
                """
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoProperties kakaoProperties;

    @Operation(
            summary = "카카오 로그인 URL 조회",
            description = """
                    프론트에서 "카카오로 시작하기" 버튼을 눌렀을 때 이동시켜야 할 카카오 인가(authorize) URL을
                    만들어 돌려준다. 서버에 설정된 `KAKAO_CLIENT_ID`, `KAKAO_REDIRECT_URI` 값으로 조립되므로
                    프론트는 별도로 카카오 키를 알 필요가 없다.

                    사용자를 이 URL로 리다이렉트하면 카카오 로그인/동의 화면이 뜨고, 완료되면 카카오가
                    `KAKAO_REDIRECT_URI`로 `code` 쿼리 파라미터를 실어 다시 리다이렉트한다. 그 `code`를
                    `POST /api/auth/kakao`로 보내야 로그인이 완성된다.
                    """
    )
    @SecurityRequirements
    @GetMapping("/kakao/login-url")
    public Map<String, String> loginUrl() {
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoProperties.clientId()
                + "&redirect_uri=" + URLEncoder.encode(kakaoProperties.redirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code";
        return Map.of("loginUrl", url);
    }

    @Operation(
            summary = "카카오 로그인 (인가 코드 교환)",
            description = """
                    카카오 콜백 페이지(`/auth/kakao/callback?code=...`)에서 받은 인가 코드(`code`)를 전달하면,
                    서버가 카카오 서버와 통신해 액세스 토큰 → 사용자 정보(닉네임, 프로필 사진)를 조회한 뒤
                    다음을 수행한다.

                    - 처음 로그인하는 카카오 계정이면 새 사용자를 생성한다 (`profileCompleted = false`).
                    - 이미 있는 계정이면 닉네임/프로필 사진을 최신화한다. 단, 온보딩(반/조/이름 등록)을
                      이미 완료한 사용자는 닉네임이 `N반_N조_이름` 형식으로 고정되어 있으므로 카카오
                      닉네임으로 덮어쓰지 않는다.
                    - 우리 서비스 자체의 `accessToken`/`refreshToken`(JWT)을 발급한다.

                    응답의 `user.profileCompleted`가 `false`라면, 프론트는 온보딩 화면으로 보내야 한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 발급"),
            @ApiResponse(responseCode = "401", description = "카카오 인증 실패 (`KAKAO_AUTH_FAILED`) - 코드가 만료/재사용되었거나 카카오 앱 설정이 잘못된 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    @PostMapping("/kakao")
    public TokenResponse kakaoLogin(@RequestBody @Valid KakaoLoginRequest request) {
        return authService.loginWithKakao(request.code());
    }

    @Operation(
            summary = "개발용 로그인 (카카오 없이 로그인)",
            description = """
                    카카오 앱 등록 전에도 화면/API 흐름을 테스트할 수 있도록 만든 로컬 전용 로그인이다.
                    닉네임만 입력하면 해당 닉네임 기반의 가상 계정으로 로그인/가입된다 (동일 닉네임 재입력 시
                    같은 계정으로 로그인됨).

                    서버 환경변수 `DEV_LOGIN_ENABLED=true`일 때만 동작하며, `local` 프로파일에서는 기본
                    활성화, `prod` 프로파일에서는 기본 비활성화되어 있다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 발급"),
            @ApiResponse(responseCode = "403", description = "개발용 로그인이 비활성화된 환경 (`DEV_LOGIN_DISABLED`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    @PostMapping("/dev-login")
    public TokenResponse devLogin(@RequestBody @Valid DevLoginRequest request) {
        return authService.devLogin(request.nickname());
    }

    @Operation(
            summary = "액세스 토큰 재발급",
            description = """
                    `accessToken`은 기본 30분으로 만료가 짧다. 만료되었거나 만료가 임박했을 때, 로그인 시
                    함께 발급받은 `refreshToken`을 이 API로 보내면 새로운 `accessToken`/`refreshToken` 쌍을
                    재발급한다 (리프레시 토큰도 회전(rotate)된다).

                    프론트는 앱 최초 진입 시에도 로컬에 저장된 `refreshToken`으로 이 API를 호출해 로그인
                    상태를 복구한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰이 유효하지 않거나 만료됨 (`INVALID_TOKEN`), 혹은 저장된 토큰을 찾을 수 없음 (`REFRESH_TOKEN_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인한 사용자의 리프레시 토큰을 서버에서 삭제한다. 액세스 토큰 자체는 만료 전까지
                    여전히 유효할 수 있으므로(무상태 JWT), 프론트는 이 API 호출과 별개로 로컬에 저장된
                    토큰을 반드시 지워야 한다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 처리 완료 (토큰이 이미 없어도 에러 없이 성공 처리)")
    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal Long userId) {
        if (userId != null) {
            authService.logout(userId);
        }
    }
}
