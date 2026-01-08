package com.mini.buting.api.friend.domain;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend",
                indexes = {
                                @Index(name = "idx_friend_member1", columnList = "member1_id"),
                                @Index(name = "idx_friend_member2", columnList = "member2_id"),
                                @Index(name = "idx_friend_became_friends_at", columnList = "became_friends_at")
                },
                uniqueConstraints = {
                                @UniqueConstraint(name = "uk_friend_pair", columnNames = {"member1_id", "member2_id"})
                }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "became_friends_at", nullable = false)
    private LocalDateTime becameFriendsAt;

    // 연관관계 - member1_id는 항상 더 작은 ID 값을 가지도록 구성
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member1_id", nullable = false)
    private Member member1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member2_id", nullable = false)
    private Member member2;

    @Builder
    public Friend(Member member1, Member member2, LocalDateTime becameFriendsAt) {
        // ID가 작은 멤버를 member1으로, 큰 멤버를 member2로 설정
        if (member1.getId() < member2.getId()) {
            this.member1 = member1;
            this.member2 = member2;
        } else {
            this.member1 = member2;
            this.member2 = member1;
        }
        this.becameFriendsAt = becameFriendsAt;
    }

    // 비즈니스 메서드
    public boolean involves(Member member) {
        return member1.equals(member) || member2.equals(member);
    }

    public Member getOtherMember(Member member) {
        if (member1.equals(member)) {
            return member2;
        } else if (member2.equals(member)) {
            return member1;
        } else {
            throw new IllegalArgumentException("해당 멤버는 이 친구 관계에 포함되지 않습니다.");
        }
    }

    // FriendRequest로부터 Friend 생성을 위한 정적 팩토리 메서드
    public static Friend fromFriendRequest(FriendRequest friendRequest) {
        return Friend.builder()
                        .member1(friendRequest.getRequester())
                        .member2(friendRequest.getReceiver())
                        .becameFriendsAt(friendRequest.getRespondedAt())
                        .build();
    }
}
