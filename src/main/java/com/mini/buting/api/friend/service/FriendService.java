package com.mini.buting.api.friend.service;

import com.mini.buting.api.friend.dto.request.FriendRequestCreateDto;
import com.mini.buting.api.friend.dto.response.FriendListResponseDto;
import com.mini.buting.api.friend.dto.response.FriendRequestResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendService {

    /**
     * 친구 요청 보내기
     */
    void sendFriendRequest(Long memberId, FriendRequestCreateDto requestDto);

    /**
     * 내가 받은 친구 요청 목록 조회
     */
    Page<FriendRequestResponseDto> getReceivedFriendRequests(Long memberId, Pageable pageable);

    /**
     * 친구 요청 수락
     */
    void acceptFriendRequest(Long memberId, Long requestId);

    /**
     * 친구 요청 거절
     */
    void rejectFriendRequest(Long memberId, Long requestId);

    /**
     * 내 친구 목록 조회
     */
    Page<FriendListResponseDto> getFriendList(Long memberId, Pageable pageable);

    /**
     * 친구 검색 (닉네임으로)
     */
    Page<FriendListResponseDto> searchFriends(Long memberId, String nickname, Pageable pageable);
}
