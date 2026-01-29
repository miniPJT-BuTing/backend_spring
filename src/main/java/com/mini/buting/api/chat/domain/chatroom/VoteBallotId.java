package com.mini.buting.api.chat.domain.chatroom;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VoteBallotId implements Serializable {

    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "member_id")
    private Long memberId;

}
