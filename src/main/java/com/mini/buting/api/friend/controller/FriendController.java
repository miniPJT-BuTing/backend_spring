package com.mini.buting.api.friend.controller;

import com.mini.buting.api.friend.dto.request.FriendRequestCreateDto;
import com.mini.buting.api.friend.dto.request.FriendRequestDto;
import com.mini.buting.api.friend.dto.response.FriendRequestResponseDto;
import com.mini.buting.api.friend.service.FriendService;
import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
                    @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1")
                    Long userId, @Valid @RequestBody FriendRequestCreateDto request) {

        log.debug("친구 요청 API 호출 - 요청자: {}, 대상: {}", userId, request.targetNickname());

        friendService.sendFriendRequest(userId, request);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 수락하기", description = "받은 친구 요청을 수락합니다.")
    @PostMapping("/requests/{friendRequestId}/accept")
    public ResponseEntity<BaseResponse<Void>> acceptFriendRequest(
                    @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1")
                    Long userId,
                    @Parameter(description = "친구 요청 ID") @PathVariable Long friendRequestId) {

        log.debug("친구 요청 수락 API 호출 - 응답자: {}, 요청ID: {}", userId, friendRequestId);

        friendService.acceptFriendRequest(userId, friendRequestId);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 거절하기", description = "받은 친구 요청을 거절합니다.")
    @PostMapping("/requests/{friendRequestId}/reject")
    public ResponseEntity<BaseResponse<Void>> rejectFriendRequest(
                    @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1")
                    Long userId,
                    @Parameter(description = "친구 요청 ID") @PathVariable Long friendRequestId) {

        log.debug("친구 요청 거절 API 호출 - 응답자: {}, 요청ID: {}", userId, friendRequestId);

        friendService.rejectFriendRequest(userId, friendRequestId);

        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    @Operation(summary = "친구 요청 목록 조회", description = "내가 받은 친구 요청 목록을 조회합니다.")
    @GetMapping("/requests")
    public ResponseEntity<BaseResponse<Page<FriendRequestResponseDto>>> getFriendRequests(
                    @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1")
                    Long userId, Pageable pageable) {

        log.debug("친구 요청 목록 조회 API 호출 - 회원: {}", userId);

        Page<FriendRequestResponseDto>
                        result = friendService.getReceivedFriendRequests(userId, pageable);

        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }
}
