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
    @Query("SELECT mr FROM MatchRequest mr " +
            "WHERE ((mr.requestTeam = :teamA AND mr.targetTeam = :teamB) " +
            "OR (mr.requestTeam = :teamB AND mr.targetTeam = :teamA)) " +
            "AND mr.status = :status")
    Optional<MatchRequest> findByTeamsAndStatus(
            @Param("teamA") Team teamA,
            @Param("teamB") Team teamB,
            @Param("status") MatchRequestStatus status
    );

    /**
     * 요청 팀 기준 목록 조회 (최신순)
     */
    List<MatchRequest> findByRequestTeamOrderByCreatedAtDesc(Team requestTeam);

    /**
     * 대상 팀 기준 목록 조회 (최신순)
     */
    List<MatchRequest> findByTargetTeamOrderByCreatedAtDesc(Team targetTeam);

    /**
     * 요청 팀 + 상태 기준 목록 조회 (최신순)
     */
    List<MatchRequest> findByRequestTeamAndStatusOrderByCreatedAtDesc(
            Team requestTeam,
            MatchRequestStatus status
    );

    /**
     * 대상 팀 + 상태 기준 목록 조회 (최신순)
     */
    List<MatchRequest> findByTargetTeamAndStatusOrderByCreatedAtDesc(
            Team targetTeam,
            MatchRequestStatus status
    );

    /**
     * 관계자 접근 가능한 단건 조회 (요청팀/대상팀 권한 체크용)
     */
    @Query("SELECT mr FROM MatchRequest mr " +
            "JOIN FETCH mr.requestTeam rt " +
            "JOIN FETCH mr.targetTeam tt " +
            "WHERE mr.id = :matchRequestId " +
            "AND (rt.id = :teamId OR tt.id = :teamId)")
    Optional<MatchRequest> findByIdAndTeamId(
            @Param("matchRequestId") Long matchRequestId,
            @Param("teamId") Long teamId
    );

    /**
     * 상세 조회용 (팀 함께 로딩)
     */
    @Query("SELECT mr FROM MatchRequest mr " +
            "JOIN FETCH mr.requestTeam " +
            "JOIN FETCH mr.targetTeam " +
            "WHERE mr.id = :matchRequestId")
    Optional<MatchRequest> findByIdWithTeams(@Param("matchRequestId") Long matchRequestId);
}
