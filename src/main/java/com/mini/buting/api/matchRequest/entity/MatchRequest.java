package com.mini.buting.api.matchRequest.entity;

import com.mini.buting.api.team.entity.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "MatchRequest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchRequestStatus status;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_team_id", nullable = false)
    private Team requestTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_team_id", nullable = false)
    private Team targetTeam;

    @Builder
    public MatchRequest(Team requestTeam, Team targetTeam) {
        this.requestTeam = requestTeam;
        this.targetTeam = targetTeam;
        this.status = MatchRequestStatus.PENDING;
    }

    // 비즈니스 메서드
    public void accept() {
        validatePendingStatus();
        this.status = MatchRequestStatus.ACCEPTED;
    }

    public void reject() {
        validatePendingStatus();
        this.status = MatchRequestStatus.REJECTED;
    }

    public void cancel() {
        validatePendingStatus();
        this.status = MatchRequestStatus.CANCELLED;
    }

    public boolean isExpired() {
        return createdAt.plusDays(7).isBefore(LocalDateTime.now());
    }

    public void expireIfNeeded() {
        if (isExpired() && status == MatchRequestStatus.PENDING) {
            this.status = MatchRequestStatus.CANCELLED;
        }
    }

    public boolean isPending() {
        return status == MatchRequestStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == MatchRequestStatus.ACCEPTED;
    }

    public boolean canBeProcessed() {
        return isPending() && !isExpired();
    }

    private void validatePendingStatus() {
        if (status != MatchRequestStatus.PENDING) {
            throw new IllegalStateException("매칭 요청 상태가 PENDING이 아닙니다. 현재 상태: " + status);
        }
        if (isExpired()) {
            throw new IllegalStateException("만료된 매칭 요청은 처리할 수 없습니다.");
        }
    }

    // Enum 정의
    public enum MatchRequestStatus {
        PENDING,    // 대기중
        ACCEPTED,   // 수락됨
        REJECTED,   // 거절됨
        CANCELLED   // 취소됨
    }
}
