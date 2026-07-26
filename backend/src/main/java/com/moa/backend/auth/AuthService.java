package com.moa.backend.auth;

import com.moa.backend.auth.dto.KakaoTokenResponse;
import com.moa.backend.auth.dto.KakaoUserInfoResponse;
import com.moa.backend.auth.dto.TokenResponse;
import com.moa.backend.global.config.AppProperties;
import com.moa.backend.global.exception.BusinessException;
import com.moa.backend.global.exception.ErrorCode;
import com.moa.backend.global.security.JwtProvider;
import com.moa.backend.user.User;
import com.moa.backend.user.UserRepository;
import com.moa.backend.user.dto.UserResponse;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final AppProperties appProperties;

    public TokenResponse loginWithKakao(String code) {
        KakaoTokenResponse kakaoToken = kakaoOAuthClient.requestToken(code);
        KakaoUserInfoResponse userInfo = kakaoOAuthClient.requestUserInfo(kakaoToken.accessToken());

        User user = userRepository.findByKakaoId(userInfo.id())
                .map(existing -> {
                    existing.updateProfile(userInfo.nickname(), userInfo.profileImageUrl());
                    return existing;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(userInfo.id())
                        .nickname(userInfo.nickname())
                        .profileImageUrl(userInfo.profileImageUrl())
                        .role(User.Role.USER)
                        .build()));

        return issueTokens(user);
    }

    /**
     * 로컬 개발 전용 로그인. 실제 카카오 앱 등록 전 UI/API 흐름을 확인할 수 있도록 제공한다.
     */
    public TokenResponse devLogin(String nickname) {
        if (!appProperties.devLoginEnabled()) {
            throw new BusinessException(ErrorCode.DEV_LOGIN_DISABLED);
        }

        long fakeKakaoId = -Math.abs((long) nickname.hashCode());
        User user = userRepository.findByKakaoId(fakeKakaoId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(fakeKakaoId)
                        .nickname(nickname)
                        .role(User.Role.USER)
                        .build()));

        return issueTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Claims claims = jwtProvider.parseClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());

        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!stored.matches(refreshToken) || stored.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return issueTokens(user);
    }

    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenExpirationMillis()));

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        existing -> existing.rotate(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .userId(user.getId())
                                .token(refreshToken)
                                .expiresAt(expiresAt)
                                .build())
                );

        return new TokenResponse(accessToken, refreshToken, UserResponse.from(user));
    }
}
