package com.moa.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserInfoResponse(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {

    public String nickname() {
        String nickname = kakaoAccount == null || kakaoAccount.profile() == null ? null : kakaoAccount.profile().nickname();
        return nickname != null ? nickname : "모공유저" + id;
    }

    public String profileImageUrl() {
        return kakaoAccount == null || kakaoAccount.profile() == null ? null : kakaoAccount.profile().profileImageUrl();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(Profile profile) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Profile(
                String nickname,
                @JsonProperty("profile_image_url") String profileImageUrl
        ) {
        }
    }
}
