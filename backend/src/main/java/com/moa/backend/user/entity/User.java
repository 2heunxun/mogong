package com.moa.backend.user.entity;

import com.moa.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false, unique = true)
    private Long kakaoId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "class_no")
    private Integer classNo;

    @Column(name = "team_no")
    private Integer teamNo;

    @Column(name = "real_name", length = 20)
    private String realName;

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    @Builder
    private User(Long kakaoId, String nickname, String profileImageUrl, Role role) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role == null ? Role.USER : role;
        this.profileCompleted = false;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        // 온보딩을 완료하면 닉네임은 "N반_N조_이름" 형식으로 고정되므로,
        // 재로그인 시 카카오 닉네임으로 덮어쓰지 않는다.
        if (!profileCompleted) {
            this.nickname = nickname;
        }
        this.profileImageUrl = profileImageUrl;
    }

    public void completeOnboarding(Integer classNo, Integer teamNo, String realName) {
        this.classNo = classNo;
        this.teamNo = teamNo;
        this.realName = realName;
        this.nickname = classNo + "반_" + teamNo + "조_" + realName;
        this.profileCompleted = true;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public enum Role {
        USER, ADMIN
    }
}
