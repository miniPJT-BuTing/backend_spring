package com.mini.buting.api.team.repository;

import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.domain.TeamInvitation;
import com.mini.buting.api.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    /**
     * 특정 팀의 모든 초대 조회
     */
    @Query("SELECT ti FROM TeamInvitation ti " + "JOIN FETCH ti.invitee " + "WHERE ti.team = :team")
    List<TeamInvitation> findByTeamWithInvitee(@Param("team") Team team);

    /**
     * 특정 멤버가 받은 대기중인 초대들 조회
     */
    @Query("SELECT ti FROM TeamInvitation ti " + "JOIN FETCH ti.team t " + "JOIN FETCH ti.inviter " + "WHERE ti.invitee = :invitee " + "AND ti.status = 'PENDING' " + "AND ti.expiredAt > :now " + "AND t.isDeleted = false")
    List<TeamInvitation> findPendingInvitationsByInvitee(@Param("invitee") Member invitee,
                    @Param("now") LocalDateTime now);

    /**
     * 팀과 초대받은 사람으로 초대 조회
     */
    Optional<TeamInvitation> findByTeamAndInvitee(Team team, Member invitee);

    /**
     * 초대 ID와 초대받은 사람으로 초대 조회 (권한 확인용)
     */
    @Query("SELECT ti FROM TeamInvitation ti " + "JOIN FETCH ti.team t " + "JOIN FETCH ti.inviter " + "WHERE ti.id = :invitationId AND ti.invitee = :invitee " + "AND t.isDeleted = false")
    Optional<TeamInvitation> findByIdAndInvitee(@Param("invitationId") Long invitationId,
                    @Param("invitee") Member invitee);

    /**
     * 특정 팀의 수락된 초대 수 조회
     */
    @Query("SELECT COUNT(ti) FROM TeamInvitation ti " + "WHERE ti.team = :team AND ti.status = 'ACCEPTED'")
    long countAcceptedInvitationsByTeam(@Param("team") Team team);

    /**
     * 만료된 대기중 초대들 조회
     */
    @Query("SELECT ti FROM TeamInvitation ti " + "WHERE ti.status = 'PENDING' " + "AND ti.expiredAt <= :now")
    List<TeamInvitation> findExpiredPendingInvitations(@Param("now") LocalDateTime now);

    /**
     * 특정 팀의 모든 초대가 수락되었는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(ti) = 0 THEN true ELSE false END " + "FROM TeamInvitation ti " + "WHERE ti.team = :team " + "AND ti.status IN ('PENDING', 'REJECTED')")
    boolean areAllInvitationsAccepted(@Param("team") Team team);
}
