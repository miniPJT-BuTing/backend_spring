package com.mini.buting.api.team.repository;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.domain.TeamMember;
import com.mini.buting.api.team.domain.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    /**
     * 팀과 멤버로 팀멤버 존재 여부 확인
     */
    boolean existsByTeamAndMember(Team team, Member member);

    /**
     * 팀의 모든 멤버 조회
     */
    List<TeamMember> findByTeam(Team team);

    /**
     * 멤버의 모든 팀 조회
     */
    List<TeamMember> findByMember(Member member);

    /**
     * 팀과 멤버로 팀멤버 삭제
     */
    void deleteByTeamAndMember(Team team, Member member);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = :team")
    long countByTeam(@Param("team") Team team);
}
