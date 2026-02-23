package com.mini.buting.api.friend.controller;

import com.mini.buting.api.friend.dto.request.FriendRequestCreateDto;
import com.mini.buting.api.friend.dto.response.FriendListResponseDto;
import com.mini.buting.api.friend.dto.response.FriendRequestResponseDto;
import com.mini.buting.api.friend.service.FriendService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Friend API", description = "친구 시스템 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기", description = "닉네임으로 다른 사용자에게 친구 요청을 보냅니다.")
    @PostMapping("/requests")
    public ResponseEntity<BaseResponse<Void>> sendFriendRequest(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody FriendRequestCreateDto request) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("친구 요청 API 호출 - 요청자: {}, 대상: {}", memberId, request.targetNickname());
        friendService.sendFriendRequest(memberId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 수락하기", description = "받은 친구 요청을 수락합니다.")
    @PostMapping("/requests/{friendRequestId}/accept")
    public ResponseEntity<BaseResponse<Void>> acceptFriendRequest(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "친구 요청 ID") @PathVariable Long friendRequestId) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("친구 요청 수락 API 호출 - 응답자: {}, 요청ID: {}", memberId, friendRequestId);

        friendService.acceptFriendRequest(memberId, friendRequestId);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 거절하기", description = "받은 친구 요청을 거절합니다.")
    @PostMapping("/requests/{friendRequestId}/reject")
    public ResponseEntity<BaseResponse<Void>> rejectFriendRequest(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "친구 요청 ID") @PathVariable Long friendRequestId) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("친구 요청 거절 API 호출 - 응답자: {}, 요청ID: {}", memberId, friendRequestId);

        friendService.rejectFriendRequest(memberId, friendRequestId);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 목록 조회", description = "내가 받은 친구 요청 목록을 조회합니다.")
    @GetMapping("/requests")
    public ResponseEntity<BaseResponse<Page<FriendRequestResponseDto>>> getFriendRequests(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("친구 요청 목록 조회 API 호출 - 회원: {}", memberId);

        Page<FriendRequestResponseDto> result = friendService.getReceivedFriendRequests(memberId, pageable);

        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }

    @Operation(summary = "내 친구 목록 조회", description = "내 친구 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<FriendListResponseDto>>> getFriendList(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable) {
        long memberId = AuthValidator.require(authUser).getId();
        log.debug("내 친구 목록 조회 API 호출 - 회원: {}", memberId);

        Page<FriendListResponseDto> result = friendService.getFriendList(memberId, pageable);

        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }
}
