package com.mini.buting.api.team.service;

import com.mini.buting.api.friend.domain.Friend;
import com.mini.buting.api.friend.repository.FriendRepository;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.api.team.domain.Gender;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    /**
     * 팀 생성
     */
    @Transactional
    public TeamResponseDto.CreateTeamResponse createTeam(Long leaderId, TeamRequestDto.CreateTeamRequest request) {
        log.info("팀 생성 요청 - leaderId: {}, teamSize: {}, inviteCount: {}", 
                leaderId, request.teamSize(), request.inviteMemberIds().size());

        // 1. 팀장 조회
        Member leader = memberRepository.findById(leaderId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 2. 팀장이 이미 다른 팀의 리더인지 확인
        if (teamRepository.existsByLeader(leader)) {
            throw new BaseException(BaseResponseStatus.ALREADY_TEAM_LEADER);
        }

        // 3. 초대할 멤버들 조회 및 검증
        List<Member> invitees = validateAndGetInvitees(leader, request.inviteMemberIds());

        // 4. 팀 생성
        Team team = Team.builder()
                .title(request.title())
                .description(request.description())
                .preferredMood(request.preferredMood())
                .teamSize(request.teamSize())
                .preferredAgeMin(request.preferredAgeMin().byteValue())
                .preferredAgeMax(request.preferredAgeMax().byteValue())
                .preferredEntryYearMin(request.preferredEntryYearMin().byteValue())
                .preferredEntryYearMax(request.preferredEntryYearMax().byteValue())
                .gender(Gender.fromMemberGender(leader.getGender()))
                .leader(leader)
                .isOpen(false) // 모든 멤버가 수락해야 true
                .build();

        Team savedTeam = teamRepository.save(team);

        // 5. 팀장을 팀 멤버로 추가
        TeamMember leaderMember = TeamMember.of(savedTeam, leader);
        savedTeam.getTeamMembers().add(leaderMember);

        // 6. 초대 생성
        List<TeamInvitation> invitations = invitees.stream()
                .map(invitee -> TeamInvitation.builder()
                        .team(savedTeam)
                        .inviter(leader)
                        .invitee(invitee)
                        .build())
                .collect(Collectors.toList());

        List<TeamInvitation> savedInvitations = teamInvitationRepository.saveAll(invitations);

        log.info("팀 생성 완료 - teamId: {}, invitationCount: {}", savedTeam.getId(), savedInvitations.size());

        // 7. 응답 생성
        return TeamResponseDto.CreateTeamResponse.builder()
                .teamId(savedTeam.getId())
                .title(savedTeam.getTitle())
                .description(savedTeam.getDescription())
                .teamSize(savedTeam.getTeamSize())
                .preferredMood(savedTeam.getPreferredMood())
                .preferredAgeMin(savedTeam.getPreferredAgeMin().intValue())
                .preferredAgeMax(savedTeam.getPreferredAgeMax().intValue())
                .preferredEntryYearMin(savedTeam.getPreferredEntryYearMin().intValue())
                .preferredEntryYearMax(savedTeam.getPreferredEntryYearMax().intValue())
                .isOpen(savedTeam.getIsOpen())
                .sentInvitations(convertToInvitationInfos(savedInvitations))
                .build();
    }

    /**
     * 친구 검색 (팀 초대용)
     */
    public TeamResponseDto.SearchFriendsResponse searchFriends(Long memberId, TeamRequestDto.SearchFriendsRequest request) {
        log.info("친구 검색 요청 - memberId: {}, keyword: {}", memberId, request.keyword());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        List<Friend> friends;
        if (request.keyword() == null || request.keyword().trim().isEmpty()) {
            friends = friendRepository.findFriendsByMember(member);
        } else {
            friends = friendRepository.findFriendsByMemberAndKeyword(member, request.keyword().trim());
        }

        List<TeamResponseDto.SearchableFriend> searchableFriends = friends.stream()
                .map(friend -> {
                    Member friendMember = friend.getOtherMember(member);
                    return TeamResponseDto.SearchableFriend.builder()
                            .memberId(friendMember.getId())
                            .nickname(friendMember.getNickname())
                            .universityEmail(friendMember.getFullUniversityEmail())
                            .bio(friendMember.getBio())
                            .age(friendMember.getAge())
                            .genderDisplayName(friendMember.getGender().getDescription())
                            .universityName(friendMember.getUniversity().getName())
                            .collegeName(friendMember.getCollege() != null ? friendMember.getCollege().getName() : null)
                            .personalityTypes(friendMember.getPersonalityCodes())
                            .build();
                })
                .toList();

        return TeamResponseDto.SearchFriendsResponse.builder()
                .friends(searchableFriends)
                .totalCount(searchableFriends.size())
                .build();
    }

    /**
     * 초대할 멤버들 검증 및 조회
     */
    private List<Member> validateAndGetInvitees(Member leader, List<Long> inviteMemberIds) {
        // 1. 자신을 초대하는지 확인
        if (inviteMemberIds.contains(leader.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_INVITE_SELF);
        }

        // 2. 초대할 멤버들 조회
        List<Member> invitees = memberRepository.findAllById(inviteMemberIds);
        if (invitees.size() != inviteMemberIds.size()) {
            throw new BaseException(BaseResponseStatus.NOT_FOUND);
        }

        // 3. 모든 초대 대상이 친구인지 확인
        for (Member invitee : invitees) {
            if (!friendRepository.existsFriendshipBetween(leader.getId(), invitee.getId())) {
                throw new BaseException(BaseResponseStatus.NOT_FRIEND_CANNOT_INVITE);
            }
        }

        return invitees;
    }

    /**
     * 초대 정보 변환
     */
    private List<TeamResponseDto.TeamInvitationInfo> convertToInvitationInfos(List<TeamInvitation> invitations) {
        return invitations.stream()
                .map(invitation -> TeamResponseDto.TeamInvitationInfo.builder()
                        .invitationId(invitation.getId())
                        .inviteeId(invitation.getInvitee().getId())
                        .inviteeName(invitation.getInvitee().getNickname())
                        .inviteeNickname(invitation.getInvitee().getNickname())
                        .status(invitation.getStatus())
                        .createdAt(invitation.getCreatedAt())
                        .expiredAt(invitation.getExpiredAt())
                        .build())
                .collect(Collectors.toList());
    }
}
