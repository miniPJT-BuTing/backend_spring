package com.mini.buting.api.chat.domain.chatroom;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@Table(
        name = "vote_ballot",
        indexes = {
                @Index(
                        name = "idx_ballot_vote_member",
                        columnList = "vote_id, member_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteBallot {

    @EmbeddedId
    private VoteBallotId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static VoteBallot create(Long voteId, Long optionId, Long memberId) {
        VoteBallot b = new VoteBallot();
        b.id = new VoteBallotId(voteId, optionId, memberId);
        return b;
    }
}
