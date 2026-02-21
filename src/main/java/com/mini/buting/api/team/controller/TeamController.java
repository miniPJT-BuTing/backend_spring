package com.mini.buting.api.team.controller;

import com.mini.buting.api.team.domain.Gender;
import com.mini.buting.api.team.domain.TeamMood;
import com.mini.buting.api.team.domain.TeamSize;
import com.mini.buting.api.team.dto.request.TeamRequestDto;
import com.mini.buting.api.team.dto.response.TeamResponseDto;
import com.mini.buting.api.team.service.TeamInvitationService;
import com.mini.buting.api.team.service.TeamService;
import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.util.AuthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/teams")
@Tag(name = "Team", description = "팀 관련 API")
public class TeamController {

    private final TeamService teamService;
    private final TeamInvitationService teamInvitationService;

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성하고 멤버들을 초대합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<TeamResponseDto.CreateTeamResponse>> createTeam(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody TeamRequestDto.CreateTeamRequest request) {
        long leaderId = AuthValidator.require(authUser).getId();
        log.info("팀 생성 API 호출 - leaderId: {}", leaderId);

        TeamResponseDto.CreateTeamResponse response = teamService.createTeam(leaderId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "친구 검색", description = "팀 초대를 위해 친구를 검색합니다.")
    @GetMapping("/friends/search")
    public ResponseEntity<BaseResponse<TeamResponseDto.SearchFriendsResponse>> searchFriends(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "검색 키워드 (닉네임 또는 이메일)") @RequestParam(required = false)
            String keyword) {
        long memberId = AuthValidator.require(authUser).getId();
        log.info("친구 검색 API 호출 - memberId: {}, keyword: {}", memberId, keyword);

        TeamRequestDto.SearchFriendsRequest request =
                TeamRequestDto.SearchFriendsRequest.builder().keyword(keyword).build();

        TeamResponseDto.SearchFriendsResponse response =
                teamService.searchFriends(memberId, request);

        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "받은 초대 목록", description = "사용자가 받은 팀 초대 목록을 조회합니다.")
    @GetMapping("/invitations/received")
    public ResponseEntity<BaseResponse<List<TeamResponseDto.TeamInvitationDetailResponse>>> getReceivedInvitations(@AuthenticationPrincipal AuthUser authUser) {
        long memberId = AuthValidator.require(authUser).getId();
        log.info("받은 초대 목록 API 호출 - memberId: {}", memberId);
        return ResponseEntity.ok(BaseResponse.onSuccess(teamInvitationService.getReceivedInvitations(memberId)));
    }

    @Operation(summary = "초대 응답", description = "팀 초대를 수락하거나 거절합니다.")
    @PatchMapping("/invitations/{invitationId}/respond")
    public ResponseEntity<BaseResponse<TeamResponseDto.RespondToInvitationResponse>> respondToInvitation(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "초대 ID", required = true) @PathVariable
            Long invitationId,
            @Valid @RequestBody TeamRequestDto.RespondToInvitationRequest request) {
        long memberId = AuthValidator.require(authUser).getId();
        log.info("초대 응답 API 호출 - memberId: {}, invitationId: {}, accept: {}", memberId,
                invitationId, request.accept());

        TeamResponseDto.RespondToInvitationResponse response =
                teamInvitationService.respondToInvitation(memberId, invitationId, request);

        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "팀 매칭글 목록 조회", description = "오픈된 팀(매칭글) 목록을 필터링하여 조회합니다.")
    @GetMapping("/matching-posts")
    public ResponseEntity<BaseResponse<Page<TeamResponseDto.TeamMatchPostSummary>>> getMatchingPosts(
            @Parameter(description = "MALE | FEMALE") @RequestParam(required = false)
            Gender gender,
            @Parameter(description = "TWO_ON_TWO | THREE_ON_THREE | ...") @RequestParam(required = false)
            TeamSize teamSize,
            @Parameter(description = "ROMANTIC_TENSION | ...") @RequestParam(required = false)
            TeamMood preferredMood,
            Pageable pageable) {

        Page<TeamResponseDto.TeamMatchPostSummary> response =
                teamService.getMatchingPosts(gender, teamSize, preferredMood, pageable);

        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "팀 매칭글 상세 조회", description = "오픈된 팀(매칭글) 상세 정보를 조회합니다.")
    @GetMapping("/matching-posts/{teamId}")
    public ResponseEntity<BaseResponse<TeamResponseDto.TeamMatchPostDetail>> getMatchingPostDetail(
            @Parameter(description = "팀(매칭글) ID", required = true) @PathVariable Long teamId) {
        TeamResponseDto.TeamMatchPostDetail response = teamService.getMatchingPostDetail(teamId);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "내가 팀장인 매칭글 수정", description = "팀장만 매칭글을 수정할 수 있습니다. (매칭 성사 전까지만 가능)")
    @PatchMapping("/matching-posts/{teamId}")
    public ResponseEntity<BaseResponse<TeamResponseDto.TeamMatchPostDetail>> updateMatchingPost(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "팀(매칭글) ID", required = true) @PathVariable Long teamId,
            @Valid @RequestBody TeamRequestDto.UpdateMatchPostRequest request) {
        long leaderId = AuthValidator.require(authUser).getId();
        TeamResponseDto.TeamMatchPostDetail response = teamService.updateMatchingPost(leaderId, teamId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "내가 팀장인 매칭글 삭제", description = "팀장만 매칭글을 삭제(해체)할 수 있습니다. (매칭 성사 전까지만 가능)")
    @DeleteMapping("/matching-posts/{teamId}")
    public ResponseEntity<BaseResponse<Void>> deleteMatchingPost(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "팀(매칭글) ID", required = true) @PathVariable Long teamId) {
        long leaderId = AuthValidator.require(authUser).getId();
        teamService.deleteMatchingPost(leaderId, teamId);
        return ResponseEntity.ok(BaseResponse.onSuccess());
    }
}
