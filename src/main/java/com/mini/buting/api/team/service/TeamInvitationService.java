package com.mini.buting.api.team.service;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.domain.TeamInvitation;
import com.mini.buting.api.team.domain.TeamMember;
import com.mini.buting.api.team.dto.request.TeamRequestDto;
import com.mini.buting.api.team.dto.response.TeamResponseDto;
import com.mini.buting.api.team.repository.TeamInvitationRepository;
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

    /**
     * 받은 초대 목록 조회
     */
    public List<TeamResponseDto.TeamInvitationDetailResponse> getReceivedInvitations(Long memberId) {
        log.info("받은 초대 목록 조회 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        List<TeamInvitation> invitations = teamInvitationRepository
                .findPendingInvitationsByInvitee(member, LocalDateTime.now());

        return invitations.stream()
                .map(this::convertToDetailResponse)
                .toList();
    }

    /**
     * 초대 응답 (수락/거절)
     */
    @Transactional
    public TeamResponseDto.RespondToInvitationResponse respondToInvitation(
            Long memberId, Long invitationId, TeamRequestDto.RespondToInvitationRequest request) {

        log.info("초대 응답 - memberId: {}, invitationId: {}, accept: {}", 
                memberId, invitationId, request.accept());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        TeamInvitation invitation = teamInvitationRepository
                .findByIdAndInvitee(invitationId, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.TEAM_INVITATION_NOT_FOUND));

        // 만료 확인 및 처리
        invitation.expireIfNeeded();

        if (!invitation.canBeProcessed()) {
            throw new BaseException(invitation.isExpired() 
                    ? BaseResponseStatus.TEAM_INVITATION_EXPIRED 
                    : BaseResponseStatus.TEAM_INVITATION_NOT_PENDING);
        }

        String responseMessage;
        if (request.accept()) {
            // 수락 처리
            invitation.accept();
            
            // 팀에 멤버 추가
            Team team = invitation.getTeam();
            TeamMember newMember = TeamMember.of(team, member);
            team.getTeamMembers().add(newMember);

            // 팀이 완성되었는지 확인하고 활성화
            checkAndActivateTeam(team);
            
            responseMessage = "팀 초대를 수락했습니다.";
        } else {
            // 거절 처리
            invitation.reject();
            responseMessage = "팀 초대를 거절했습니다.";
        }

        log.info("초대 응답 완료 - invitationId: {}, status: {}", 
                invitation.getId(), invitation.getStatus());

        return TeamResponseDto.RespondToInvitationResponse.builder()
                .invitationId(invitation.getId())
                .status(invitation.getStatus())
                .message(responseMessage)
                .build();
    }

    /**
     * 팀 활성화 확인 및 처리
     */
    private void checkAndActivateTeam(Team team) {
        // 모든 초대가 수락되었고, 팀원 수가 목표에 도달했는지 확인
        boolean allInvitationsAccepted = teamInvitationRepository.areAllInvitationsAccepted(team);
        boolean teamSizeReached = team.getCurrentMemberCount() == team.getTeamSize().getSize();

        if (allInvitationsAccepted && teamSizeReached) {
            team.activate();
            log.info("팀 활성화 - teamId: {}, memberCount: {}", 
                    team.getId(), team.getCurrentMemberCount());
        }
    }

    /**
     * 만료된 초대들 정리 (배치 작업용)
     */
    @Transactional
    public void expireOldInvitations() {
        List<TeamInvitation> expiredInvitations = teamInvitationRepository
                .findExpiredPendingInvitations(LocalDateTime.now());

        expiredInvitations.forEach(TeamInvitation::expireIfNeeded);
        
        log.info("만료된 초대 정리 완료 - count: {}", expiredInvitations.size());
    }

    /**
     * 초대 상세 응답 변환
     */
    private TeamResponseDto.TeamInvitationDetailResponse convertToDetailResponse(TeamInvitation invitation) {
        Team team = invitation.getTeam();
        Member inviter = invitation.getInviter();

        return TeamResponseDto.TeamInvitationDetailResponse.builder()
                .invitationId(invitation.getId())
                .teamInfo(TeamResponseDto.TeamInfo.builder()
                        .teamId(team.getId())
                        .title(team.getTitle())
                        .description(team.getDescription())
                        .teamSize(team.getTeamSize())
                        .preferredMood(team.getPreferredMood())
                        .preferredAgeMin(team.getPreferredAgeMin().intValue())
                        .preferredAgeMax(team.getPreferredAgeMax().intValue())
                        .preferredEntryYearMin(team.getPreferredEntryYearMin().intValue())
                        .preferredEntryYearMax(team.getPreferredEntryYearMax().intValue())
                        .currentMemberCount(team.getCurrentMemberCount())
                        .targetMemberCount(team.getTeamSize().getSize())
                        .build())
                .inviterInfo(TeamResponseDto.MemberInfo.builder()
                        .memberId(inviter.getId())
                        .nickname(inviter.getNickname())
                        .age(inviter.getAge())
                        .bio(inviter.getBio())
                        .universityName(inviter.getUniversity().getName())
                        .collegeName(inviter.getCollege() != null ? inviter.getCollege().getName() : null)
                        .build())
                .status(invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .expiredAt(invitation.getExpiredAt())
                .isExpired(invitation.isExpired())
                .build();
    }
}
