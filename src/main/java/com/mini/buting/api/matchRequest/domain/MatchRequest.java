package com.mini.buting.api.matchRequest.domain;

import com.mini.buting.api.team.domain.Team;
import com.mini.buting.global.common.BaseTimeEntity;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "MatchRequest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // 7일 후 요청 만료, 향후 수정 예정
    public boolean isExpired() {
        return getCreatedAt().plusDays(7).isBefore(LocalDateTime.now());
    }

    public void expireIfNeeded() {
        if (isExpired() && status == MatchRequestStatus.PENDING) {
            this.status = MatchRequestStatus.REJECTED;
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
            throw new BaseException(BaseResponseStatus.MATCH_REQUEST_NOT_PENDING);
        }
        if (isExpired()) {
            throw new BaseException(BaseResponseStatus.MATCH_REQUEST_EXPIRED);
        }
    }

    // Enum 정의
    public enum MatchRequestStatus {
        PENDING,    // 대기중
        ACCEPTED,   // 수락됨
        REJECTED   // 거절됨
    }
}
