package com.mini.buting.api.team.domain;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.global.common.BaseTimeEntity;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_invitation",
        indexes = {
                @Index(name = "idx_team_invitation_team", columnList = "team_id"),
                @Index(name = "idx_team_invitation_inviter", columnList = "inviter_id"),
                @Index(name = "idx_team_invitation_invitee", columnList = "invitee_id"),
                @Index(name = "idx_team_invitation_status", columnList = "status"),
                @Index(name = "idx_team_invitation_created", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_invitation_pair", 
                                columnNames = {"team_id", "invitee_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("팀 초대 관리 테이블")
public class TeamInvitation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("초대 식별자")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("초대 상태")
    private InvitationStatus status;

    @Column(name = "responded_at")
    @Comment("응답 시간")
    private LocalDateTime respondedAt;

    @Column(name = "expired_at")
    @Comment("만료 시간")
    private LocalDateTime expiredAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false,
                foreignKey = @ForeignKey(name = "FK_team_invitation_team"))
    @Comment("초대된 팀")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false,
                foreignKey = @ForeignKey(name = "FK_team_invitation_inviter"))
    @Comment("초대를 보낸 사람 (팀장)")
    private Member inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false,
                foreignKey = @ForeignKey(name = "FK_team_invitation_invitee"))
    @Comment("초대받은 사람")
    private Member invitee;

    @Builder
    public TeamInvitation(Team team, Member inviter, Member invitee) {
        this.team = team;
        this.inviter = inviter;
        this.invitee = invitee;
        this.status = InvitationStatus.PENDING;
        this.expiredAt = LocalDateTime.now().plusDays(7); // 7일 후 만료
    }

    // 비즈니스 메서드
    public void accept() {
        validatePendingStatus();
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        validatePendingStatus();
        this.status = InvitationStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public void expireIfNeeded() {
        if (isExpired() && status == InvitationStatus.PENDING) {
            this.status = InvitationStatus.EXPIRED;
            this.respondedAt = LocalDateTime.now();
        }
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }

    public boolean canBeProcessed() {
        return isPending() && !isExpired();
    }

    public boolean involves(Member member) {
        return inviter.equals(member) || invitee.equals(member);
    }

    public boolean isInviter(Member member) {
        return inviter.equals(member);
    }

    public boolean isInvitee(Member member) {
        return invitee.equals(member);
    }

    private void validatePendingStatus() {
        if (status != InvitationStatus.PENDING) {
            throw new BaseException(BaseResponseStatus.TEAM_INVITATION_NOT_PENDING);
        }
        if (isExpired()) {
            throw new BaseException(BaseResponseStatus.TEAM_INVITATION_EXPIRED);
        }
    }

    // Enum 정의
    public enum InvitationStatus {
        PENDING,    // 대기중
        ACCEPTED,   // 수락됨
        REJECTED,   // 거절됨
        EXPIRED     // 만료됨
    }
}
