package com.mini.buting.api.friend.controller;

import com.mini.buting.api.friend.dto.request.FriendRequestCreateDto;
import com.mini.buting.api.friend.dto.request.FriendRequestDto;
import com.mini.buting.api.friend.service.FriendService;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Friend API", description = "친구 시스템 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기", description = "닉네임으로 다른 사용자에게 친구 요청을 보냅니다.")
    @PostMapping("/requests")
    public ResponseEntity<BaseResponse<Void>> sendFriendRequest(
                    @Parameter(description = "현재 로그인한 사용자 정보", hidden = true)
                    @SessionAttribute("currentMember") Member currentMember,
                    @Valid @RequestBody FriendRequestCreateDto request) {

        log.info("친구 요청 API 호출 - 요청자: {}, 대상: {}", currentMember.getNickname(),
                        request.targetNickname());

        friendService.sendFriendRequest(currentMember.getId(), request);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 수락하기", description = "받은 친구 요청을 수락합니다.")
    @PostMapping("/requests/{friendRequestId}/accept")
    public ResponseEntity<BaseResponse<Void>> acceptFriendRequest(
                    @Parameter(description = "현재 로그인한 사용자 정보", hidden = true)
                    @SessionAttribute("currentMember") Member currentMember,
                    @Parameter(description = "친구 요청 ID") @PathVariable Long friendRequestId) {

        log.info("친구 요청 수락 API 호출 - 응답자: {}, 요청ID: {}", currentMember.getNickname(),
                        friendRequestId);

        friendService.acceptFriendRequest(currentMember.getId(), friendRequestId);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 거절하기", description = "받은 친구 요청을 거절합니다.")
    @PostMapping("/requests/{friendRequestId}/reject")
    public ResponseEntity<BaseResponse<Void>> rejectFriendRequest(
                    @Parameter(description = "현재 로그인한 사용자 정보", hidden = true)
                    @SessionAttribute("currentMember") Member currentMember,
                    @Parameter(description = "친구 요청 ID") @PathVariable Long friendRequestId) {

        log.info("친구 요청 거절 API 호출 - 응답자: {}, 요청ID: {}", currentMember.getNickname(),
                        friendRequestId);

        friendService.rejectFriendRequest(currentMember.getId(), friendRequestId);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 목록 조회", description = "내가 받은 친구 요청 목록을 조회합니다.")
    @GetMapping("/requests")
    public ResponseEntity<BaseResponse<FriendRequestDto.FriendRequestListResponse>> getFriendRequests(
                    @Parameter(description = "현재 로그인한 사용자 정보", hidden = true)
                    @SessionAttribute("currentMember") Member currentMember, Pageable pageable) {

        log.info("친구 요청 목록 조회 API 호출 - 회원: {}", currentMember.getNickname());

        FriendRequestDto.FriendRequestListResponse result =
                        (FriendRequestDto.FriendRequestListResponse) friendService.getReceivedFriendRequests(currentMember.getId(), pageable);

        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }
}
