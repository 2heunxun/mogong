package com.moa.backend.weekendparty.entity;

import com.moa.backend.global.exception.BusinessException;
import com.moa.backend.global.exception.ErrorCode;
import com.moa.backend.global.jpa.BaseTimeEntity;
import com.moa.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weekend_party")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeekendParty extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(name = "meeting_at", nullable = false)
    private LocalDateTime meetingAt;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "open_chat_url", nullable = false, length = 500)
    private String openChatUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "recruitment_type", nullable = false, length = 20)
    private RecruitmentType recruitmentType;

    @Builder
    private WeekendParty(User owner, String title, String description, String category, LocalDateTime meetingAt,
                          Integer capacity, String openChatUrl, RecruitmentType recruitmentType) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.category = category;
        this.meetingAt = meetingAt;
        this.capacity = capacity;
        this.openChatUrl = openChatUrl;
        this.recruitmentType = recruitmentType;
        this.status = Status.RECRUITING;
    }

    public void update(String title, String description, String category, LocalDateTime meetingAt,
                        Integer capacity, String openChatUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.meetingAt = meetingAt;
        this.capacity = capacity;
        this.openChatUrl = openChatUrl;
    }

    public boolean isOwner(Long userId) {
        return this.owner.getId().equals(userId);
    }

    public boolean isRecruiting() {
        return this.status == Status.RECRUITING;
    }

    public void validateOwner(Long userId) {
        if (!isOwner(userId)) {
            throw new BusinessException(ErrorCode.NOT_WEEKEND_PARTY_OWNER);
        }
    }

    public void close() {
        this.status = Status.CLOSED;
    }

    public void reopen() {
        this.status = Status.RECRUITING;
    }

    public boolean isFirstCome() {
        return this.recruitmentType == RecruitmentType.FIRST_COME;
    }

    public enum Status {
        RECRUITING, CLOSED
    }

    public enum RecruitmentType {
        FIRST_COME, APPROVAL
    }
}
