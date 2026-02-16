package com.mini.buting.api.team.repository;

import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mini.buting.api.team.domain.Gender;
import com.mini.buting.api.team.domain.TeamMood;
import com.mini.buting.api.team.domain.TeamSize;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 특정 멤버가 팀장으로 있는 팀 조회
     */
    Optional<Team> findByLeaderAndIsDeletedFalse(Member leader);

    /**
     * 특정 멤버가 팀장으로 있는 팀 존재 여부 확인
     */
    boolean existsByLeaderAndIsDeletedFalse(Member leader);

    /**
     * 팀 단건 조회 (삭제된 팀 제외)
     */
    Optional<Team> findByIdAndIsDeletedFalse(Long teamId);

    /**
     * 오픈된 팀(=매칭글) 단건 조회 (삭제 제외)
     */
    Optional<Team> findByIdAndIsOpenTrueAndIsDeletedFalse(Long teamId);

    /**
     * 팀과 해당 팀의 멤버들을 함께 조회
     */
    @Query("SELECT t FROM Team t " + "LEFT JOIN FETCH t.teamMembers tm " + "LEFT JOIN FETCH tm.member " + "WHERE t.id = :teamId AND t.isDeleted = false")
    Optional<Team> findByIdWithMembers(@Param("teamId") Long teamId);

    /**
     * 팀과 팀장 정보를 함께 조회
     */
    @Query("SELECT t FROM Team t " + "JOIN FETCH t.leader " + "WHERE t.id = :teamId AND t.isDeleted = false")
    Optional<Team> findByIdWithLeader(@Param("teamId") Long teamId);

    /**
     * 매칭글 목록 조회 (오픈 + 삭제 제외 + 옵션 필터)
     */
    @Query("""
                    SELECT t
                    FROM Team t
                    WHERE t.isOpen = true
                      AND t.isDeleted = false
                      AND (:gender IS NULL OR t.gender = :gender)
                      AND (:teamSize IS NULL OR t.teamSize = :teamSize)
                      AND (:preferredMood IS NULL OR t.preferredMood = :preferredMood)
                    """)
    Page<Team> findMatchingPosts(@Param("gender") Gender gender,
                    @Param("teamSize") TeamSize teamSize,
                    @Param("preferredMood") TeamMood preferredMood, Pageable pageable);
}
