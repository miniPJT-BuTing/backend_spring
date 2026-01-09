package com.mini.buting.api.friend.domain;

import com.mini.buting.api.member.domain.Member;
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
@Table(name = "friend_request",
        indexes = {
                @Index(name = "idx_friend_request_requester", columnList = "requester_id"),
                @Index(name = "idx_friend_request_receiver", columnList = "receiver_id"),
                @Index(name = "idx_friend_request_status", columnList = "status"),
                @Index(name = "idx_friend_request_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_friend_request_pair", columnNames = {"requester_id", "receiver_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;  // 친구 요청을 보낸 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;   // 친구 요청을 받은 사람

    @Builder
    public FriendRequest(Member requester, Member receiver) {
        this.requester = requester;
        this.receiver = receiver;
        this.status = RequestStatus.PENDING;
    }

    // 비즈니스 메서드
    public void accept() {
        validatePendingStatus();
        this.status = RequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        validatePendingStatus();
        this.status = RequestStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return getCreatedAt().plusDays(7).isBefore(LocalDateTime.now());
    }

    public void expireIfNeeded() {
        if (isExpired() && status == RequestStatus.PENDING) {
            this.status = RequestStatus.EXPIRED;
            this.respondedAt = LocalDateTime.now();
        }
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == RequestStatus.ACCEPTED;
    }

    public boolean canBeProcessed() {
        return isPending() && !isExpired();
    }

    public boolean involves(Member member) {
        return requester.equals(member) || receiver.equals(member);
    }

    public boolean isRequester(Member member) {
        return requester.equals(member);
    }

    public boolean isReceiver(Member member) {
        return receiver.equals(member);
    }

    private void validatePendingStatus() {
        if (status != RequestStatus.PENDING) {
            throw new BaseException(BaseResponseStatus.FRIEND_REQUEST_NOT_PENDING);
        }
        if (isExpired()) {
            throw new BaseException(BaseResponseStatus.FRIEND_REQUEST_EXPIRED);
        }
    }

    // Enum 정의
    public enum RequestStatus {
        PENDING,    // 대기중
        ACCEPTED,   // 수락됨
        REJECTED,   // 거절됨
        EXPIRED     // 만료됨
    }
}
