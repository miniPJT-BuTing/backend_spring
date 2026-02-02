package com.mini.buting.api.matchRequest.service;

import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import com.mini.buting.api.chat.service.ChatRoomService;
import com.mini.buting.api.matchRequest.domain.MatchRequest;
import com.mini.buting.api.matchRequest.domain.MatchRequest.MatchRequestStatus;
import com.mini.buting.api.matchRequest.dto.request.MatchRequestRequestDto;
import com.mini.buting.api.matchRequest.dto.response.MatchRequestResponseDto;
import com.mini.buting.api.matchRequest.repository.MatchRequestRepository;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.api.team.domain.Gender;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.api.team.repository.TeamMemberRepository;
import com.mini.buting.api.team.repository.TeamRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;

    @Transactional
    public MatchRequestResponseDto.MatchRequestCreateResponse createMatchRequest(Long memberId,
                    MatchRequestRequestDto.CreateMatchRequest request) {
        // 요청자 조회
        Member member = getMember(memberId);

        // 요청 팀은 팀장 기준으로 식별
        Team requestTeam = teamRepository.findByLeaderAndIsDeletedFalse(member)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_TEAM_LEADER));

        // 매칭 가능한 상태인지 확인
        validateTeamReady(requestTeam);

        // 대상 팀 조회
        Team targetTeam = teamRepository.findByIdAndIsDeletedFalse(request.targetTeamId())
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.TEAM_NOT_FOUND));

        // 자기 팀으로는 요청 불가
        if (requestTeam.getId().equals(targetTeam.getId())) {
            throw new BaseException(BaseResponseStatus.MATCH_REQUEST_SELF);
        }

        // 성별 매칭 제한
        if (requestTeam.getGender() == targetTeam.getGender()) {
            throw new BaseException(BaseResponseStatus.MATCH_REQUEST_SAME_GENDER);
        }

        // 대상 팀도 매칭 가능한 상태인지 확인
        validateTeamReady(targetTeam);

        // 기존 요청(대기/수락) 중복 방지
        if (matchRequestRepository.findByTeamsAndStatus(requestTeam, targetTeam,
                        MatchRequestStatus.PENDING).isPresent()) {
            throw new BaseException(BaseResponseStatus.MATCH_REQUEST_ALREADY_EXISTS);
        }

        if (matchRequestRepository.findByTeamsAndStatus(requestTeam, targetTeam,
                        MatchRequestStatus.ACCEPTED).isPresent()) {
            throw new BaseException(BaseResponseStatus.MATCH_REQUEST_ALREADY_EXISTS);
        }

        // 이미 채팅방이 존재하면 요청 불가
        Team maleTeam = requestTeam.getGender() == Gender.MALE ? requestTeam : targetTeam;
        Team femaleTeam = requestTeam.getGender() == Gender.MALE ? targetTeam : requestTeam;

        if (chatRoomRepository.existsByMaleTeamAndFemaleTeam(maleTeam, femaleTeam)) {
            throw new BaseException(BaseResponseStatus.CHATROOM_ALREADY_EXISTS);
        }

        // 매칭 요청 생성
        MatchRequest matchRequest =
                        MatchRequest.builder().requestTeam(requestTeam).targetTeam(targetTeam)
                                        .build();

        MatchRequest saved = matchRequestRepository.save(matchRequest);

        return MatchRequestResponseDto.MatchRequestCreateResponse.builder()
                        .matchRequestId(saved.getId()).status(saved.getStatus().name())
                        .requestTeamId(requestTeam.getId()).targetTeamId(targetTeam.getId())
                        .createdAt(saved.getCreatedAt()).build();
    }

    @Transactional
    public MatchRequestResponseDto.MatchRequestRespondResponse respondMatchRequest(Long memberId,
                    Long matchRequestId, MatchRequestRequestDto.RespondMatchRequest request) {
        // 응답자(대상 팀 리더) 확인
        Member member = getMember(memberId);
        Team leaderTeam = teamRepository.findByLeaderAndIsDeletedFalse(member)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_TEAM_LEADER));

        // 요청 및 연관 팀 로딩
        MatchRequest matchRequest = matchRequestRepository.findByIdWithTeams(matchRequestId)
                        .orElseThrow(() -> new BaseException(
                                        BaseResponseStatus.MATCH_REQUEST_NOT_FOUND));

        // 대상 팀 리더만 응답 가능
        if (!leaderTeam.getId().equals(matchRequest.getTargetTeam().getId())) {
            throw new BaseException(BaseResponseStatus.NOT_TEAM_LEADER);
        }

        // 만료/상태 검증
        matchRequest.expireIfNeeded();
        if (!matchRequest.canBeProcessed()) {
            throw new BaseException(matchRequest.isExpired() ?
                            BaseResponseStatus.MATCH_REQUEST_EXPIRED :
                            BaseResponseStatus.MATCH_REQUEST_NOT_PENDING);
        }

        Long chatRoomId = null;
        if (Boolean.TRUE.equals(request.accept())) {
            // 수락 시 채팅방 생성 + 환영 메시지
            matchRequest.accept();
            matchRequestRepository.save(matchRequest);

            ChatRoom room = chatRoomService.createRoom(matchRequest);
            chatRoomService.sendWelcomeMessage(room.getRoomId(), room.getLeader().getId());
            chatRoomId = room.getRoomId();
        } else {
            // 거절 처리
            matchRequest.reject();
            matchRequestRepository.save(matchRequest);
        }

        return MatchRequestResponseDto.MatchRequestRespondResponse.builder()
                        .matchRequestId(matchRequest.getId())
                        .status(matchRequest.getStatus().name()).chatRoomId(chatRoomId)
                        .updatedAt(matchRequest.getUpdatedAt()).build();
    }

    public MatchRequestResponseDto.MatchRequestDetailResponse getMatchRequest(Long memberId,
                    Long matchRequestId) {
        // 요청자 팀(리더) 기준 권한 확인
        Member member = getMember(memberId);
        Team leaderTeam = teamRepository.findByLeaderAndIsDeletedFalse(member)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_TEAM_LEADER));

        // 요청 팀 또는 대상 팀만 조회 가능
        MatchRequest matchRequest =
                        matchRequestRepository.findByIdAndTeamId(matchRequestId, leaderTeam.getId())
                                        .orElseThrow(() -> new BaseException(
                                                        BaseResponseStatus.MATCH_REQUEST_NOT_FOUND));

        return MatchRequestResponseDto.MatchRequestDetailResponse.builder()
                        .matchRequestId(matchRequest.getId())
                        .status(matchRequest.getStatus().name())
                        .requestTeam(toTeamSummary(matchRequest.getRequestTeam()))
                        .targetTeam(toTeamSummary(matchRequest.getTargetTeam()))
                        .createdAt(matchRequest.getCreatedAt())
                        .updatedAt(matchRequest.getUpdatedAt()).expired(matchRequest.isExpired())
                        .build();
    }

    public MatchRequestResponseDto.MatchRequestListResponse getMatchRequests(Long memberId,
                    String type, String status) {
        // 팀 리더 기준 목록 조회
        Member member = getMember(memberId);
        Team leaderTeam = teamRepository.findByLeaderAndIsDeletedFalse(member)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_TEAM_LEADER));

        // 상태 파라미터 파싱
        MatchRequestStatus parsedStatus = parseStatus(status);

        List<MatchRequest> requests;
        if ("sent".equalsIgnoreCase(type)) {
            // 보낸 요청 목록
            requests = parsedStatus == null ?
                            matchRequestRepository.findByRequestTeamWithTeamsOrderByCreatedAtDesc(
                                            leaderTeam) :
                            matchRequestRepository.findByRequestTeamAndStatusWithTeamsOrderByCreatedAtDesc(
                                            leaderTeam, parsedStatus);
        } else if ("received".equalsIgnoreCase(type)) {
            // 받은 요청 목록
            requests = parsedStatus == null ?
                            matchRequestRepository.findByTargetTeamWithTeamsOrderByCreatedAtDesc(
                                            leaderTeam) :
                            matchRequestRepository.findByTargetTeamAndStatusWithTeamsOrderByCreatedAtDesc(
                                            leaderTeam, parsedStatus);
        } else {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        List<MatchRequestResponseDto.MatchRequestSummary> summaries =
                        requests.stream().map(matchRequest -> toSummary(matchRequest, type))
                                        .toList();

        return MatchRequestResponseDto.MatchRequestListResponse.builder().requests(summaries)
                        .totalCount(summaries.size()).build();
    }

    private MatchRequestResponseDto.MatchRequestSummary toSummary(MatchRequest matchRequest,
                    String type) {
        Team opponentTeam = "received".equalsIgnoreCase(type) ?
                        matchRequest.getRequestTeam() :
                        matchRequest.getTargetTeam();
        return MatchRequestResponseDto.MatchRequestSummary.builder()
                        .matchRequestId(matchRequest.getId())
                        .status(matchRequest.getStatus().name())
                        .requestedAtAgo(formatTimeAgo(matchRequest.getCreatedAt()))
                        .opponentTeamTitle(opponentTeam.getTitle())
                        .opponentTeamSize(opponentTeam.getTeamSize())
                        .opponentPreferredMood(opponentTeam.getPreferredMood().getDisplayName())
                        .opponentPreferredEntryYearMin(
                                        opponentTeam.getPreferredEntryYearMin().intValue())
                        .opponentPreferredEntryYearMax(
                                        opponentTeam.getPreferredEntryYearMax().intValue()).build();
    }

    private MatchRequestResponseDto.TeamSummary toTeamSummary(Team team) {
        // 현재 인원은 TeamMember 기준 카운트
        int currentMemberCount = (int) teamMemberRepository.countByTeam(team);
        return MatchRequestResponseDto.TeamSummary.builder().teamId(team.getId())
                        .title(team.getTitle()).teamSize(team.getTeamSize())
                        .gender(team.getGender()).currentMemberCount(currentMemberCount)
                        .targetMemberCount(team.getTeamSize().getSize()).build();
    }

    private MatchRequestStatus parseStatus(String status) {
        // null/blank면 필터 미적용
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return MatchRequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }
    }

    private Member getMember(Long memberId) {
        // 활성 회원 조회
        return memberRepository.findById(memberId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));
    }

    private String formatTimeAgo(java.time.LocalDateTime createdAt) {
        java.time.Duration duration =
                        java.time.Duration.between(createdAt, java.time.LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + "분 전";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }
        long days = duration.toDays();
        return days + "일 전";
    }

    private void validateTeamReady(Team team) {
        // 팀 상태가 매칭 가능해야 함
        if (!team.canRequestMatch()) {
            throw new BaseException(BaseResponseStatus.TEAM_NOT_READY);
        }
    }
}
