package com.mini.buting.api.matchRequest.repository;

import com.mini.buting.api.matchRequest.domain.MatchRequest;
import com.mini.buting.api.matchRequest.domain.MatchRequest.MatchRequestStatus;
import com.mini.buting.api.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    /**
     * 요청 팀/대상 팀 조합으로 특정 상태 요청 조회 (양방향)
     */
    @Query("SELECT mr FROM MatchRequest mr " + "WHERE ((mr.requestTeam = :teamA AND mr.targetTeam = :teamB) " + "OR (mr.requestTeam = :teamB AND mr.targetTeam = :teamA)) " + "AND mr.status = :status " + "AND mr.requestTeam.isDeleted = false AND mr.targetTeam.isDeleted = false")
    Optional<MatchRequest> findByTeamsAndStatus(@Param("teamA") Team teamA,
                    @Param("teamB") Team teamB, @Param("status") MatchRequestStatus status);

    /**
     * 요청 팀 기준 목록 조회 (최신순)
     */
    @Query("SELECT mr FROM MatchRequest mr " + "JOIN FETCH mr.requestTeam " + "JOIN FETCH mr.targetTeam " + "WHERE mr.requestTeam = :requestTeam " + "AND mr.requestTeam.isDeleted = false AND mr.targetTeam.isDeleted = false " + "ORDER BY mr.createdAt DESC")
    List<MatchRequest> findByRequestTeamWithTeamsOrderByCreatedAtDesc(
                    @Param("requestTeam") Team requestTeam);

    /**
     * 대상 팀 기준 목록 조회 (최신순)
     */
    @Query("SELECT mr FROM MatchRequest mr " + "JOIN FETCH mr.requestTeam " + "JOIN FETCH mr.targetTeam " + "WHERE mr.targetTeam = :targetTeam " + "AND mr.requestTeam.isDeleted = false AND mr.targetTeam.isDeleted = false " + "ORDER BY mr.createdAt DESC")
    List<MatchRequest> findByTargetTeamWithTeamsOrderByCreatedAtDesc(
                    @Param("targetTeam") Team targetTeam);

    /**
     * 요청 팀 + 상태 기준 목록 조회 (최신순)
     */
    @Query("SELECT mr FROM MatchRequest mr " + "JOIN FETCH mr.requestTeam " + "JOIN FETCH mr.targetTeam " + "WHERE mr.requestTeam = :requestTeam " + "AND mr.status = :status " + "AND mr.requestTeam.isDeleted = false AND mr.targetTeam.isDeleted = false " + "ORDER BY mr.createdAt DESC")
    List<MatchRequest> findByRequestTeamAndStatusWithTeamsOrderByCreatedAtDesc(
                    @Param("requestTeam") Team requestTeam,
                    @Param("status") MatchRequestStatus status);

    /**
     * 대상 팀 + 상태 기준 목록 조회 (최신순)
     */
    @Query("SELECT mr FROM MatchRequest mr " + "JOIN FETCH mr.requestTeam " + "JOIN FETCH mr.targetTeam " + "WHERE mr.targetTeam = :targetTeam " + "AND mr.status = :status " + "AND mr.requestTeam.isDeleted = false AND mr.targetTeam.isDeleted = false " + "ORDER BY mr.createdAt DESC")
    List<MatchRequest> findByTargetTeamAndStatusWithTeamsOrderByCreatedAtDesc(
                    @Param("targetTeam") Team targetTeam,
                    @Param("status") MatchRequestStatus status);

    /**
     * 관계자 접근 가능한 단건 조회 (요청팀/대상팀 권한 체크용)
     */
    @Query("SELECT mr FROM MatchRequest mr " + "JOIN FETCH mr.requestTeam rt " + "JOIN FETCH mr.targetTeam tt " + "WHERE mr.id = :matchRequestId " + "AND (rt.id = :teamId OR tt.id = :teamId) " + "AND rt.isDeleted = false AND tt.isDeleted = false")
    Optional<MatchRequest> findByIdAndTeamId(@Param("matchRequestId") Long matchRequestId,
                    @Param("teamId") Long teamId);

    /**
     * 상세 조회용 (팀 함께 로딩)
     */
    @Query("SELECT mr FROM MatchRequest mr " + "JOIN FETCH mr.requestTeam " + "JOIN FETCH mr.targetTeam " + "WHERE mr.id = :matchRequestId " + "AND mr.requestTeam.isDeleted = false AND mr.targetTeam.isDeleted = false")
    Optional<MatchRequest> findByIdWithTeams(@Param("matchRequestId") Long matchRequestId);

    /**
     * 해당 팀이 매칭 성사(ACCEPTED)된 적이 있는지 확인 (요청/대상 양방향)
     */
    @Query("""
                    SELECT CASE WHEN COUNT(mr) > 0 THEN true ELSE false END
                    FROM MatchRequest mr
                    WHERE mr.status = 'ACCEPTED'
                      AND (mr.requestTeam.id = :teamId OR mr.targetTeam.id = :teamId)
                    """)
    boolean existsAcceptedByTeamId(@Param("teamId") Long teamId);
}
