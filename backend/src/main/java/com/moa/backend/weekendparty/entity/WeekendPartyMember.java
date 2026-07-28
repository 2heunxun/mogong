package com.moa.backend.weekendparty.entity;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weekend_party_member", uniqueConstraints = @UniqueConstraint(name = "uk_weekend_party_member", columnNames = {"party_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeekendPartyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private WeekendParty party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Builder
    private WeekendPartyMember(WeekendParty party, User user, Status status) {
        this.party = party;
        this.user = user;
        this.status = status == null ? Status.PENDING : status;
    }

    @PrePersist
    void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = Status.APPROVED;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
