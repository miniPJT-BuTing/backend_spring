package com.mini.buting.api.team.repository;

import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 특정 멤버가 팀장으로 있는 팀 조회
     */
    Optional<Team> findByLeader(Member leader);

    /**
     * 특정 멤버가 팀장으로 있는 팀 존재 여부 확인
     */
    boolean existsByLeader(Member leader);

    /**
     * 팀과 해당 팀의 멤버들을 함께 조회
     */
    @Query("SELECT t FROM Team t " +
           "LEFT JOIN FETCH t.teamMembers tm " +
           "LEFT JOIN FETCH tm.member " +
           "WHERE t.id = :teamId")
    Optional<Team> findByIdWithMembers(@Param("teamId") Long teamId);

    /**
     * 팀과 팀장 정보를 함께 조회
     */
    @Query("SELECT t FROM Team t " +
           "JOIN FETCH t.leader " +
           "WHERE t.id = :teamId")
    Optional<Team> findByIdWithLeader(@Param("teamId") Long teamId);
}
