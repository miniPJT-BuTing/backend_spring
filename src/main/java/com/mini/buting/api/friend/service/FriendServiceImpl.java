package com.mini.buting.api.friend.service;

import com.mini.buting.api.friend.domain.Friend;
import com.mini.buting.api.friend.domain.FriendRequest;
import com.mini.buting.api.friend.dto.request.FriendRequestCreateDto;
import com.mini.buting.api.friend.dto.response.FriendListResponseDto;
import com.mini.buting.api.friend.dto.response.FriendRequestResponseDto;
import com.mini.buting.api.friend.repository.FriendRepository;
import com.mini.buting.api.friend.repository.FriendRequestRepository;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendServiceImpl implements FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void sendFriendRequest(Long memberId, FriendRequestCreateDto requestDto) {
        // 요청자 조회
        Member requester = getMemberById(memberId);

        // 대상자 조회 (닉네임으로)
        Member receiver = memberRepository.findByNickname(requestDto.targetNickname())
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        // 자기 자신에게 요청 방지
        if (requester.equals(receiver)) {
            throw new BaseException(BaseResponseStatus.FRIEND_REQUEST_TO_SELF);
        }

        // 이미 친구인지 확인
        if (friendRepository.existsFriendshipBetween(requester.getId(), receiver.getId())) {
            throw new BaseException(BaseResponseStatus.ALREADY_FRIENDS);
        }

        // 기존 친구 요청이 있는지 확인 (양방향)
        if (friendRequestRepository.findPendingRequestBetweenMembers(requester.getId(), receiver.getId()).isPresent()) {
            throw new BaseException(BaseResponseStatus.FRIEND_REQUEST_ALREADY_EXISTS);
        }

        // 친구 요청 생성
        FriendRequest friendRequest = FriendRequest.builder()
                        .requester(requester)
                        .receiver(receiver)
                        .build();

        friendRequestRepository.save(friendRequest);
        log.info("친구 요청 생성 완료: {} -> {}", requester.getNickname(), receiver.getNickname());
    }

    @Override
    public Page<FriendRequestResponseDto> getReceivedFriendRequests(Long memberId, Pageable pageable) {
        // 멤버 존재 확인
        getMemberById(memberId);

        // 받은 친구 요청 목록 조회
        Page<FriendRequest> requests = friendRequestRepository.findPendingRequestsByReceiver(memberId, pageable);

        return requests.map(FriendRequestResponseDto::from);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long memberId, Long requestId) {
        // 친구 요청 조회 및 권한 확인
        FriendRequest friendRequest = getFriendRequestWithAuth(requestId, memberId);

        // 요청 받은 사람만 수락 가능
        if (!friendRequest.isReceiver(getMemberById(memberId))) {
            throw new BaseException(BaseResponseStatus.NO_PERMISSION_TO_RESPOND);
        }

        // 친구 요청 수락 처리
        friendRequest.accept();

        // Friend 관계 생성
        Friend friendship = Friend.fromFriendRequest(friendRequest);
        friendRepository.save(friendship);

        log.info("친구 요청 수락 완료: {} -> {}",
                        friendRequest.getRequester().getNickname(),
                        friendRequest.getReceiver().getNickname());
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long memberId, Long requestId) {
        // 친구 요청 조회 및 권한 확인
        FriendRequest friendRequest = getFriendRequestWithAuth(requestId, memberId);

        // 요청 받은 사람만 거절 가능
        if (!friendRequest.isReceiver(getMemberById(memberId))) {
            throw new BaseException(BaseResponseStatus.NO_PERMISSION_TO_RESPOND);
        }

        // 친구 요청 거절 처리
        friendRequest.reject();

        log.info("친구 요청 거절 완료: {} -> {}",
                        friendRequest.getRequester().getNickname(),
                        friendRequest.getReceiver().getNickname());
    }

    @Override
    public Page<FriendListResponseDto> getFriendList(Long memberId, Pageable pageable) {
        // 멤버 존재 확인
        Member member = getMemberById(memberId);

        // 친구 목록 조회
        Page<Friend> friendships = friendRepository.findFriendsByMemberId(memberId, pageable);

        return friendships.map(friendship -> FriendListResponseDto.from(friendship, member));
    }

    @Override
    public Page<FriendListResponseDto> searchFriends(Long memberId, String nickname, Pageable pageable) {
        // 멤버 존재 확인
        Member member = getMemberById(memberId);

        // 친구 검색
        Page<Friend> friendships = friendRepository.searchFriendsByNickname(memberId, nickname, pageable);

        return friendships.map(friendship -> FriendListResponseDto.from(friendship, member));
    }

    // 유틸리티 메서드
    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }

    private FriendRequest getFriendRequestWithAuth(Long requestId, Long memberId) {
        return friendRequestRepository.findByIdAndMemberId(requestId, memberId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.FRIEND_REQUEST_NOT_FOUND));
    }
}
