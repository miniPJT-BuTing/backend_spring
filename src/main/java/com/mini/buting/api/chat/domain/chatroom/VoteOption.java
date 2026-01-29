package com.mini.buting.api.chat.domain.chatroom;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "vote_option",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vote_option_order",
                        columnNames = {"vote_id", "option_order"}
                )
        },
        indexes = {
                @Index(name = "idx_vote_option_vote", columnList = "vote_id")
        }
)
public class VoteOption {

    @Id
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vote_id", nullable = false, updatable = false)
    private Vote vote;

    @Column(name = "option_text", nullable = false, length = 50)
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    public static VoteOption create(Long optionId, Vote vote, String text, int order) {
        VoteOption o = new VoteOption();
        o.optionId = optionId;
        o.vote = vote;
        o.optionText = text;
        o.optionOrder = order;
        return o;
    }
}
