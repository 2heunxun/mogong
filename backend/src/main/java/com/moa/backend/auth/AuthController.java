package com.moa.backend.auth;

import com.moa.backend.auth.dto.DevLoginRequest;
import com.moa.backend.auth.dto.KakaoLoginRequest;
import com.moa.backend.auth.dto.RefreshRequest;
import com.moa.backend.auth.dto.TokenResponse;
import com.moa.backend.global.config.KakaoProperties;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoProperties kakaoProperties;

    @GetMapping("/kakao/login-url")
    public Map<String, String> loginUrl() {
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoProperties.clientId()
                + "&redirect_uri=" + URLEncoder.encode(kakaoProperties.redirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code";
        return Map.of("loginUrl", url);
    }

    @PostMapping("/kakao")
    public TokenResponse kakaoLogin(@RequestBody @Valid KakaoLoginRequest request) {
        return authService.loginWithKakao(request.code());
    }

    @PostMapping("/dev-login")
    public TokenResponse devLogin(@RequestBody @Valid DevLoginRequest request) {
        return authService.devLogin(request.nickname());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal Long userId) {
        if (userId != null) {
            authService.logout(userId);
        }
    }
}
