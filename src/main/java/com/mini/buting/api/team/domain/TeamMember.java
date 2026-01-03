package com.mini.buting.api.team.domain;

import com.mini.buting.api.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TeamMember")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(TeamMemberId.class)
public class TeamMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", foreignKey = @ForeignKey(name = "FK_Team_TO_TeamMember_1"))
    private Team team;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_member_TO_TeamMember_1"))
    private Member member;

    // 팀원을 팀에 추가하는 정적 팩토리 메서드
    public static TeamMember of(Team team, Member member) {
        return TeamMember.builder()
                .team(team)
                .member(member)
                .build();
    }
}
