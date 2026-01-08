
package com.mini.buting.api.chat.domain.chatroom;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.team.domain.Team;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom {
    @Id
    @Comment("채팅방 식별자")
    private Long roomId;

    @Column(name = "title", nullable = false, length = 10)
    @Comment("채팅방 이름")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "male_team_id", nullable = false, updatable = false)
    @Comment("남자 팀")
    private Team maleTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "female_team_id", nullable = false, updatable = false)
    @Comment("여자 팀")
    private Team femaleTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_id", nullable = false, updatable = false)
    @Comment("채팅방 대표자(방장)")
    private Member leader;

    @Column(name = "member_count", nullable = false)
    @Comment("채팅방 참여자 수")
    private Integer memberCount;

    @Embedded
    @Comment("채팅방의 마지막 메시지")
    private LastMessage lastMessage;

    @Builder
    private ChatRoom(
            Long roomId,
            String title,
            Team maleTeam,
            Team femaleTeam,
            Member leader,
            Integer memberCount,
            LastMessage lastMessage
    ) {
        this.roomId = roomId;
        this.title = title;
        this.maleTeam = maleTeam;
        this.femaleTeam = femaleTeam;
        this.leader = leader;
        this.memberCount = memberCount;
        this.lastMessage = lastMessage;
    }
}


