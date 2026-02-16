package com.mini.buting.api.team.service;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.domain.TeamInvitation;
import com.mini.buting.api.team.domain.TeamMember;
import com.mini.buting.api.team.dto.request.TeamRequestDto;
import com.mini.buting.api.team.dto.response.TeamResponseDto;
import com.mini.buting.api.team.repository.TeamInvitationRepository;
import com.mini.buting.api.team.repository.TeamMemberRepository;
import com.mini.buting.api.team.repository.TeamRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeamInvitationService {

    private final TeamInvitationRepository teamInvitationRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * 받은 초대 목록 조회
     */
    public List<TeamResponseDto.TeamInvitationDetailResponse> getReceivedInvitations(
                    Long memberId) {
        log.debug("받은 초대 목록 조회 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        List<TeamInvitation> invitations =
                        teamInvitationRepository.findPendingInvitationsByInvitee(member,
                                        LocalDateTime.now());

        return invitations.stream().map(this::convertToDetailResponse).toList();
    }

    /**
     * 초대 응답 (수락/거절) - 완전 수정 버전
     */
    @Transactional
    public TeamResponseDto.RespondToInvitationResponse respondToInvitation(Long memberId,
                    Long invitationId, TeamRequestDto.RespondToInvitationRequest request) {

        log.debug("초대 응답 - memberId: {}, invitationId: {}, accept: {}", memberId, invitationId,
                        request.accept());

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        TeamInvitation invitation =
                        teamInvitationRepository.findByIdAndInvitee(invitationId, member)
                                        .orElseThrow(() -> new BaseException(
                                                        BaseResponseStatus.TEAM_INVITATION_NOT_FOUND));

        // 만료 확인 및 처리
        invitation.expireIfNeeded();

        if (!invitation.canBeProcessed()) {
            throw new BaseException(invitation.isExpired() ?
                            BaseResponseStatus.TEAM_INVITATION_EXPIRED :
                            BaseResponseStatus.TEAM_INVITATION_NOT_PENDING);
        }

        String responseMessage;
        if (request.accept()) {
            // 1. 초대 수락 처리
            invitation.accept();
            teamInvitationRepository.save(invitation);

            // 2. TeamMemberRepository로 직접 저장 (컬렉션 조작 X)
            Team team = invitation.getTeam();
            TeamMember newMember = TeamMember.of(team, member);
            teamMemberRepository.save(newMember);  // 직접 저장!

            // 3. 팀이 완성되었는지 확인하고 활성화
            checkAndActivateTeam(team);

            responseMessage = "팀 초대를 수락했습니다.";
            log.debug("팀 멤버 추가 완료 - teamId: {}, memberId: {}", team.getId(), member.getId());

        } else {
            // 거절 처리
            invitation.reject();
            teamInvitationRepository.save(invitation);
            responseMessage = "팀 초대를 거절했습니다.";
        }

        log.debug("초대 응답 완료 - invitationId: {}, status: {}", invitation.getId(),
                        invitation.getStatus());

        return TeamResponseDto.RespondToInvitationResponse.builder()
                        .invitationId(invitation.getId()).status(invitation.getStatus())
                        .message(responseMessage).build();
    }

    /**
     * 팀 활성화 확인 및 처리 - 디버깅 버전
     */
    private void checkAndActivateTeam(Team team) {
        log.debug("=== 팀 활성화 체크 시작 - teamId: {} ===", team.getId());

        // 1. 모든 초대가 수락되었는지 확인
        boolean allInvitationsAccepted = teamInvitationRepository.areAllInvitationsAccepted(team);
        log.debug("모든 초대 수락 여부: {}", allInvitationsAccepted);

        // 2. 현재 멤버 수 조회
        long currentMemberCount = teamMemberRepository.countByTeam(team);
        long targetMemberCount = team.getTeamSize().getSize();
        log.debug("현재 멤버 수: {} / 목표 멤버 수: {}", currentMemberCount, targetMemberCount);

        // 3. 팀 현재 상태 확인
        log.debug("팀 현재 오픈 상태: {}", team.getIsOpen());

        // 4. 각 조건별 체크
        log.debug("조건1 - 모든 초대 수락: {}", allInvitationsAccepted);
        log.debug("조건2 - 멤버 수 일치: {} == {} = {}", currentMemberCount, targetMemberCount,
                        currentMemberCount == targetMemberCount);
        log.debug("조건3 - 아직 비활성: {}", !team.getIsOpen());

        if (allInvitationsAccepted && currentMemberCount == targetMemberCount && !team.getIsOpen()) {
            log.debug("모든 조건 만족, 팀 활성화 실행");
            team.activate();
            teamRepository.save(team);

            log.debug("팀 활성화 완료! - teamId: {}, memberCount: {}/{}", team.getId(),
                            currentMemberCount, targetMemberCount);
        } else {
            log.debug("팀 활성화 조건 미달성 - teamId: {}", team.getId());

            // 상세한 미달성 이유 출력
            if (!allInvitationsAccepted) {
                log.debug("  - 이유: 아직 수락되지 않은 초대가 있음");
            }
            if (currentMemberCount != targetMemberCount) {
                log.debug("  - 이유: 멤버 수 불일치 ({}/{})", currentMemberCount, targetMemberCount);
            }
            if (team.getIsOpen()) {
                log.debug("  - 이유: 이미 활성화된 팀");
            }
        }

        log.debug("=== 팀 활성화 체크 종료 ===");
    }

    /**
     * 만료된 초대들 정리 (배치 작업용)
     */
    @Transactional
    public void expireOldInvitations() {
        List<TeamInvitation> expiredInvitations =
                        teamInvitationRepository.findExpiredPendingInvitations(LocalDateTime.now());

        expiredInvitations.forEach(TeamInvitation::expireIfNeeded);
        teamInvitationRepository.saveAll(expiredInvitations);

        log.debug("만료된 초대 정리 완료 - count: {}", expiredInvitations.size());
    }

    /**
     * 초대 상세 응답 변환
     */
    private TeamResponseDto.TeamInvitationDetailResponse convertToDetailResponse(
                    TeamInvitation invitation) {
        Team team = invitation.getTeam();
        Member inviter = invitation.getInviter();

        return TeamResponseDto.TeamInvitationDetailResponse.builder()
                        .invitationId(invitation.getId())
                        .teamInfo(TeamResponseDto.TeamInfo.builder().teamId(team.getId())
                                        .title(team.getTitle()).description(team.getDescription())
                                        .teamSize(team.getTeamSize())
                                        .preferredMood(team.getPreferredMood())
                                        .preferredAgeMin(team.getPreferredAgeMin().intValue())
                                        .preferredAgeMax(team.getPreferredAgeMax().intValue())
                                        .preferredEntryYearMin(
                                                        team.getPreferredEntryYearMin().intValue())
                                        .preferredEntryYearMax(
                                                        team.getPreferredEntryYearMax().intValue())
                                        .currentMemberCount(team.getCurrentMemberCount())
                                        .targetMemberCount(team.getTeamSize().getSize()).build())
                        .inviterInfo(TeamResponseDto.MemberInfo.builder().memberId(inviter.getId())
                                        .nickname(inviter.getNickname()).age(inviter.getAge())
                                        .bio(inviter.getBio())
                                        .universityName(inviter.getUniversity().getName())
                                        .collegeName(inviter.getCollege() != null ?
                                                        inviter.getCollege().getName() :
                                                        null).build())
                        .status(invitation.getStatus()).createdAt(invitation.getCreatedAt())
                        .expiredAt(invitation.getExpiredAt()).isExpired(invitation.isExpired())
                        .build();
    }
}
