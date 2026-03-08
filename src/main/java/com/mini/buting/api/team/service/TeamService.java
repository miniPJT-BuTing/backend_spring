package com.mini.buting.api.team.service;

import com.mini.buting.api.friend.domain.Friend;
import com.mini.buting.api.friend.repository.FriendRepository;
import com.mini.buting.api.matchRequest.repository.MatchRequestRepository;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.api.team.domain.Gender;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.domain.TeamInvitation;
import com.mini.buting.api.team.domain.TeamMember;
import com.mini.buting.api.team.domain.TeamMood;
import com.mini.buting.api.team.domain.TeamSize;
import com.mini.buting.api.team.dto.request.TeamRequestDto;
import com.mini.buting.api.team.dto.response.TeamResponseDto;
import com.mini.buting.api.team.repository.TeamInvitationRepository;
import com.mini.buting.api.team.repository.TeamMemberRepository;
import com.mini.buting.api.team.repository.TeamRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final TeamMemberRepository teamMemberRepository;
    private final MatchRequestRepository matchRequestRepository;

    /**
     * 팀 생성
     */
    @Transactional
    public TeamResponseDto.CreateTeamResponse createTeam(Long leaderId,
                    TeamRequestDto.CreateTeamRequest request) {
        log.debug("팀 생성 요청 - leaderId: {}, teamSize: {}, inviteCount: {}", leaderId,
                        request.teamSize(), request.inviteMemberIds().size());

        // 1. 팀장 조회
        Member leader = memberRepository.findById(leaderId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        // 2. 팀장이 이미 다른 팀의 리더인지 확인
        if (teamRepository.existsByLeaderAndIsDeletedFalse(leader)) {
            throw new BaseException(BaseResponseStatus.ALREADY_TEAM_LEADER);
        }

        // 3. 초대할 멤버들 조회 및 검증
        List<Member> invitees = validateAndGetInvitees(leader, request.inviteMemberIds());

        // 4. 팀 생성
        Team team = Team.builder().title(request.title()).description(request.description())
                        .preferredMood(request.preferredMood()).teamSize(request.teamSize())
                        .preferredAgeMin(request.preferredAgeMin().byteValue())
                        .preferredAgeMax(request.preferredAgeMax().byteValue())
                        .preferredEntryYearMin(request.preferredEntryYearMin().byteValue())
                        .preferredEntryYearMax(request.preferredEntryYearMax().byteValue())
                        .gender(Gender.fromMemberGender(leader.getGender())).leader(leader)
                        .isOpen(false) // 모든 멤버가 수락해야 true
                        .build();

        Team savedTeam = teamRepository.save(team);

        // 5. 팀장을 팀 멤버로 추가 - 직접 저장
        TeamMember leaderMember = TeamMember.of(savedTeam, leader);
        teamMemberRepository.save(leaderMember);  // 직접 저장!
        log.debug("팀장 멤버 추가 완료 - teamId: {}, leaderId: {}", savedTeam.getId(), leader.getId());

        // 6. 초대 생성
        List<TeamInvitation> invitations = invitees.stream()
                        .map(invitee -> TeamInvitation.builder().team(savedTeam).inviter(leader)
                                        .invitee(invitee).build()).collect(Collectors.toList());

        List<TeamInvitation> savedInvitations = teamInvitationRepository.saveAll(invitations);

        log.debug("팀 생성 완료 - teamId: {}, invitationCount: {}", savedTeam.getId(),
                        savedInvitations.size());

        // 7. 응답 생성
        return TeamResponseDto.CreateTeamResponse.builder().teamId(savedTeam.getId())
                        .title(savedTeam.getTitle()).description(savedTeam.getDescription())
                        .teamSize(savedTeam.getTeamSize())
                        .preferredMood(savedTeam.getPreferredMood())
                        .preferredAgeMin(savedTeam.getPreferredAgeMin().intValue())
                        .preferredAgeMax(savedTeam.getPreferredAgeMax().intValue())
                        .preferredEntryYearMin(savedTeam.getPreferredEntryYearMin().intValue())
                        .preferredEntryYearMax(savedTeam.getPreferredEntryYearMax().intValue())
                        .isOpen(savedTeam.getIsOpen())
                        .sentInvitations(convertToInvitationInfos(savedInvitations)).build();
    }

    /**
     * 친구 검색 (팀 초대용) - 같은 성별만 필터링
     */
    public TeamResponseDto.SearchFriendsResponse searchFriends(Long memberId,
                    TeamRequestDto.SearchFriendsRequest request) {
        log.debug("친구 검색 요청 - memberId: {}, keyword: {}", memberId, request.keyword());

        Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        List<Friend> friends;
        if (request.keyword() == null || request.keyword().trim().isEmpty()) {
            friends = friendRepository.findFriendsByMember(member);
        } else {
            friends = friendRepository.findFriendsByMemberAndKeyword(member,
                            request.keyword().trim());
        }

        // 수정: 같은 성별만 필터링
        List<TeamResponseDto.SearchableFriend> searchableFriends =
                        friends.stream().map(friend -> friend.getOtherMember(member))
                                        .filter(friendMember -> friendMember.getGender() == member.getGender()) // 같은 성별 필터링
                                        .map(friendMember -> TeamResponseDto.SearchableFriend.builder()
                                                        .memberId(friendMember.getId())
                                                        .nickname(friendMember.getNickname())
                                                        .universityEmail(
                                                                        friendMember.getFullUniversityEmail())
                                                        .bio(friendMember.getBio())
                                                        .age(friendMember.getAge())
                                                        .genderDisplayName(friendMember.getGender()
                                                                        .getDescription())
                                                        .universityName(friendMember.getUniversity()
                                                                        .getName())
                                                        .collegeName(friendMember.getCollege() != null ?
                                                                        friendMember.getCollege()
                                                                                        .getName() :
                                                                        null).personalityTypes(
                                                                        friendMember.getPersonalityCodes())
                                                        .build()).toList();

        return TeamResponseDto.SearchFriendsResponse.builder().friends(searchableFriends)
                        .totalCount(searchableFriends.size()).build();
    }

    /**
     * 초대 응답 (수락/거절)
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
            // 수락 처리
            invitation.accept();
            teamInvitationRepository.save(invitation);

            // TeamMemberRepository를 통한 안전한 저장
            Team team = invitation.getTeam();

            // 이미 팀 멤버인지 확인 (중복 방지)
            if (!teamMemberRepository.existsByTeamAndMember(team, member)) {
                TeamMember newMember = TeamMember.of(team, member);
                teamMemberRepository.save(newMember);
                log.debug("새 팀 멤버 추가 - teamId: {}, memberId: {}", team.getId(), member.getId());
            }

            // 팀이 완성되었는지 확인하고 활성화
            checkAndOpenTeam(team);

            responseMessage = "팀 초대를 수락했습니다.";
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
     * 모든 초대가 수락되었는지 확인하고 팀을 오픈 상태로 변경
     */
    @Transactional
    public void checkAndOpenTeam(Team team) {
        // 더 효율적으로 모든 초대가 수락되었는지 확인
        boolean allAccepted = teamInvitationRepository.areAllInvitationsAccepted(team);

        if (allAccepted && !team.getIsOpen()) {
            // 팀을 오픈 상태로 변경
            team.activate();
            teamRepository.save(team);

            log.debug("팀 오픈 상태 변경 - teamId: {}, 모든 초대가 수락됨", team.getId());
        }
    }

    /**
     * 팀 매칭글(=오픈 팀) 목록 조회
     */
    public Page<TeamResponseDto.TeamMatchPostSummary> getMatchingPosts(Gender gender, TeamSize teamSize,
                    TeamMood preferredMood, Pageable pageable) {
        Page<Team> page = teamRepository.findMatchingPosts(gender, teamSize, preferredMood, pageable);

        List<Long> teamIds = page.getContent().stream().map(Team::getId).toList();
        Map<Long, Long> memberCountMap = new HashMap<>();
        if (!teamIds.isEmpty()) {
            teamMemberRepository.countMembersByTeamIds(teamIds).forEach(row -> memberCountMap.put(
                            row.getTeamId(), row.getMemberCount()));
        }

        return page.map(team -> {
            long current = memberCountMap.getOrDefault(team.getId(), 0L);
            return TeamResponseDto.TeamMatchPostSummary.builder().teamId(team.getId())
                            .title(team.getTitle()).teamSize(team.getTeamSize())
                            .gender(team.getGender())
                            .preferredMood(team.getPreferredMood().getDisplayName())
                            .preferredAgeMin(team.getPreferredAgeMin().intValue())
                            .preferredAgeMax(team.getPreferredAgeMax().intValue())
                            .preferredEntryYearMin(team.getPreferredEntryYearMin().intValue())
                            .preferredEntryYearMax(team.getPreferredEntryYearMax().intValue())
                            .currentMemberCount((int) current)
                            .targetMemberCount(team.getTeamSize().getSize())
                            .createdAt(team.getCreatedAt()).build();
        });
    }

    /**
     * 내가 속한 팀 목록 조회 (팀장 우선)
     */
    public List<TeamResponseDto.MyTeamSummary> getMyTeams(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        List<TeamMember> memberships = teamMemberRepository.findActiveByMemberWithTeamAndLeader(member);
        if (memberships.isEmpty()) {
            return List.of();
        }

        Map<Long, Team> uniqueTeams = new HashMap<>();
        for (TeamMember membership : memberships) {
            Team team = membership.getTeam();
            if (team == null || Boolean.TRUE.equals(team.getIsDeleted())) continue;
            uniqueTeams.putIfAbsent(team.getId(), team);
        }

        List<Team> sortedTeams = uniqueTeams.values().stream()
                .sorted(Comparator
                        .comparing((Team team) -> !team.getLeader().getId().equals(memberId))
                        .thenComparing(Team::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<Long> teamIds = sortedTeams.stream().map(Team::getId).toList();
        Map<Long, Long> memberCountMap = new HashMap<>();
        if (!teamIds.isEmpty()) {
            teamMemberRepository.countMembersByTeamIds(teamIds)
                    .forEach(row -> memberCountMap.put(row.getTeamId(), row.getMemberCount()));
        }

        return sortedTeams.stream()
                .map(team -> {
                    long currentMemberCount = memberCountMap.getOrDefault(team.getId(), 0L);
                    String role = team.getLeader().getId().equals(memberId) ? "LEADER" : "MEMBER";

                    return TeamResponseDto.MyTeamSummary.builder()
                            .teamId(team.getId())
                            .role(role)
                            .title(team.getTitle())
                            .teamSize(team.getTeamSize())
                            .preferredMood(team.getPreferredMood().getDisplayName())
                            .preferredAgeMin(team.getPreferredAgeMin().intValue())
                            .preferredAgeMax(team.getPreferredAgeMax().intValue())
                            .preferredEntryYearMin(team.getPreferredEntryYearMin().intValue())
                            .preferredEntryYearMax(team.getPreferredEntryYearMax().intValue())
                            .currentMemberCount((int) currentMemberCount)
                            .targetMemberCount(team.getTeamSize().getSize())
                            .isOpen(team.getIsOpen())
                            .createdAt(team.getCreatedAt())
                            .build();
                })
                .toList();
    }

    /**
     * 팀 매칭글(=오픈 팀) 상세 조회
     */
    public TeamResponseDto.TeamMatchPostDetail getMatchingPostDetail(Long teamId) {
        Team team = teamRepository.findByIdAndIsOpenTrueAndIsDeletedFalse(teamId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.TEAM_NOT_FOUND));
        return toMatchPostDetail(team);
    }

    /**
     * 팀장 전용: 매칭글 수정 (매칭 성사 전까지만 허용)
     */
    @Transactional
    public TeamResponseDto.TeamMatchPostDetail updateMatchingPost(Long leaderId, Long teamId,
                    TeamRequestDto.UpdateMatchPostRequest request) {
        Team team = teamRepository.findByIdWithLeader(teamId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.TEAM_NOT_FOUND));

        if (!team.getLeader().getId().equals(leaderId)) {
            throw new BaseException(BaseResponseStatus.NOT_TEAM_LEADER);
        }

        // 매칭 성사 후에는 수정 불가
        if (matchRequestRepository.existsAcceptedByTeamId(teamId)) {
            throw new BaseException(BaseResponseStatus.TEAM_ALREADY_MATCHED);
        }

        boolean hasAny = request.title() != null || request.description() != null
                        || request.preferredMood() != null || request.preferredAgeMin() != null
                        || request.preferredAgeMax() != null || request.preferredEntryYearMin() != null
                        || request.preferredEntryYearMax() != null;
        if (!hasAny) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        // 범위 쌍 검증
        if ((request.preferredAgeMin() == null) != (request.preferredAgeMax() == null)) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
        if (request.preferredAgeMin() != null && request.preferredAgeMin() > request.preferredAgeMax()) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
        if ((request.preferredEntryYearMin() == null) != (request.preferredEntryYearMax() == null)) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
        if (request.preferredEntryYearMin() != null
                        && request.preferredEntryYearMin() > request.preferredEntryYearMax()) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        if (request.title() != null) {
            team.updateTitle(request.title());
        }
        if (request.description() != null) {
            team.updateDescription(request.description());
        }
        if (request.preferredMood() != null) {
            team.updatePreferredMood(request.preferredMood());
        }
        if (request.preferredAgeMin() != null) {
            team.updatePreferredAgeRange(request.preferredAgeMin(), request.preferredAgeMax());
        }
        if (request.preferredEntryYearMin() != null) {
            team.updatePreferredEntryYearRange(request.preferredEntryYearMin(),
                            request.preferredEntryYearMax());
        }

        teamRepository.save(team);
        return toMatchPostDetail(team);
    }

    /**
     * 팀장 전용: 매칭글 삭제(해체) - 매칭 성사 전까지만 허용
     */
    @Transactional
    public void deleteMatchingPost(Long leaderId, Long teamId) {
        Team team = teamRepository.findByIdWithLeader(teamId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.TEAM_NOT_FOUND));

        if (!team.getLeader().getId().equals(leaderId)) {
            throw new BaseException(BaseResponseStatus.NOT_TEAM_LEADER);
        }

        if (matchRequestRepository.existsAcceptedByTeamId(teamId)) {
            throw new BaseException(BaseResponseStatus.TEAM_ALREADY_MATCHED);
        }

        team.softDelete();
        teamRepository.save(team);
    }

    private TeamResponseDto.TeamMatchPostDetail toMatchPostDetail(Team team) {
        long currentMemberCount = teamMemberRepository.countByTeam(team);

        Member leader = team.getLeader();
        TeamResponseDto.MemberInfo leaderInfo = TeamResponseDto.MemberInfo.builder()
                        .memberId(leader.getId()).nickname(leader.getNickname()).age(leader.getAge())
                        .bio(leader.getBio())
                        .universityName(leader.getUniversity() != null ?
                                        leader.getUniversity().getName() :
                                        null)
                        .collegeName(leader.getCollege() != null ? leader.getCollege().getName() : null)
                        .build();

        return TeamResponseDto.TeamMatchPostDetail.builder().teamId(team.getId())
                        .title(team.getTitle()).description(team.getDescription())
                        .teamSize(team.getTeamSize()).gender(team.getGender())
                        .preferredMood(team.getPreferredMood().getDisplayName())
                        .preferredAgeMin(team.getPreferredAgeMin().intValue())
                        .preferredAgeMax(team.getPreferredAgeMax().intValue())
                        .preferredEntryYearMin(team.getPreferredEntryYearMin().intValue())
                        .preferredEntryYearMax(team.getPreferredEntryYearMax().intValue())
                        .currentMemberCount((int) currentMemberCount)
                        .targetMemberCount(team.getTeamSize().getSize()).leaderInfo(leaderInfo)
                        .createdAt(team.getCreatedAt()).updatedAt(team.getUpdatedAt()).build();
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
            throw new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND);
        }

        // 3. 모든 초대 대상이 친구인지 확인
        for (Member invitee : invitees) {
            if (!friendRepository.existsFriendshipBetween(leader.getId(), invitee.getId())) {
                throw new BaseException(BaseResponseStatus.NOT_FRIEND_CANNOT_INVITE);
            }
        }

        // 4. 모든 초대 대상이 같은 성별인지 확인
        for (Member invitee : invitees) {
            if (invitee.getGender() != leader.getGender()) {
                throw new BaseException(BaseResponseStatus.DIFFERENT_GENDER_CANNOT_INVITE);
            }
        }

        return invitees;
    }

    /**
     * 초대 정보 변환
     */
    private List<TeamResponseDto.TeamInvitationInfo> convertToInvitationInfos(
                    List<TeamInvitation> invitations) {
        return invitations.stream().map(invitation -> TeamResponseDto.TeamInvitationInfo.builder()
                        .invitationId(invitation.getId()).inviteeId(invitation.getInvitee().getId())
                        .inviteeName(invitation.getInvitee().getNickname())
                        .inviteeNickname(invitation.getInvitee().getNickname())
                        .status(invitation.getStatus()).createdAt(invitation.getCreatedAt())
                        .expiredAt(invitation.getExpiredAt()).build()).collect(Collectors.toList());
    }
}
