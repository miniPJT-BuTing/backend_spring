package com.mini.buting.api.matchRequest.controller;

import com.mini.buting.api.matchRequest.dto.request.MatchRequestRequestDto;
import com.mini.buting.api.matchRequest.dto.response.MatchRequestResponseDto;
import com.mini.buting.api.matchRequest.service.MatchRequestService;
import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.util.AuthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MatchRequest", description = "팀 매칭 요청 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/match-requests")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @Operation(summary = "매칭 요청 생성", description = "대상 팀에게 매칭 요청을 보냅니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<MatchRequestResponseDto.MatchRequestCreateResponse>> createMatchRequest(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody MatchRequestRequestDto.CreateMatchRequest request) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("매칭 요청 생성 - memberId: {}, targetTeamId: {}", memberId, request.targetTeamId());
        MatchRequestResponseDto.MatchRequestCreateResponse response =
                matchRequestService.createMatchRequest(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "매칭 요청 응답", description = "매칭 요청을 수락하거나 거절합니다.")
    @PatchMapping("/{matchRequestId}/respond")
    public ResponseEntity<BaseResponse<MatchRequestResponseDto.MatchRequestRespondResponse>> respondMatchRequest(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "매칭 요청 ID", required = true) @PathVariable
            Long matchRequestId,
            @Valid @RequestBody MatchRequestRequestDto.RespondMatchRequest request) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("매칭 요청 응답 - memberId: {}, matchRequestId: {}, accept: {}", memberId,
                matchRequestId, request.accept());
        MatchRequestResponseDto.MatchRequestRespondResponse response =
                matchRequestService.respondMatchRequest(memberId, matchRequestId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "매칭 요청 단건 조회", description = "매칭 요청 상세 정보를 조회합니다.")
    @GetMapping("/{matchRequestId}")
    public ResponseEntity<BaseResponse<MatchRequestResponseDto.MatchRequestDetailResponse>> getMatchRequest(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "매칭 요청 ID", required = true) @PathVariable
            Long matchRequestId) {
        long memberId = AuthValidator.require(authUser).getId();
        MatchRequestResponseDto.MatchRequestDetailResponse response =
                matchRequestService.getMatchRequest(memberId, matchRequestId);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @Operation(summary = "매칭 요청 목록 조회", description = "보낸/받은 매칭 요청 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<MatchRequestResponseDto.MatchRequestListResponse>> getMatchRequests(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "sent | received", required = true) @RequestParam
            String type, @Parameter(description = "PENDING | ACCEPTED | REJECTED")
            @RequestParam(required = false) String status) {
        long memberId = AuthValidator.require(authUser).getId();
        MatchRequestResponseDto.MatchRequestListResponse response =
                matchRequestService.getMatchRequests(memberId, type, status);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }
}
