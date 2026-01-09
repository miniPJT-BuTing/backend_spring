package com.mini.buting.api.friend.repository;

import com.mini.buting.api.friend.domain.FriendRequest;
import com.mini.buting.api.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    /**
     * 특정 사용자가 받은 친구 요청 목록 조회 (최신순)
     */
    @Query("SELECT fr FROM FriendRequest fr " +
                    "JOIN FETCH fr.requester " +
                    "WHERE fr.receiver.id = :receiverId " +
                    "AND fr.status = com.mini.buting.api.friend.domain.FriendRequest.RequestStatus.PENDING " +
                    "ORDER BY fr.createdAt DESC")
    Page<FriendRequest> findPendingRequestsByReceiver(@Param("receiverId") Long receiverId, Pageable pageable);

    /**
     * 두 사용자 간의 기존 친구 요청 확인 (양방향)
     */
    @Query("SELECT fr FROM FriendRequest fr " +
                    "WHERE ((fr.requester.id = :memberId1 AND fr.receiver.id = :memberId2) " +
                    "    OR (fr.requester.id = :memberId2 AND fr.receiver.id = :memberId1)) " +
                    "AND fr.status = com.mini.buting.api.friend.domain.FriendRequest.RequestStatus.PENDING")
    Optional<FriendRequest> findPendingRequestBetweenMembers(@Param("memberId1") Long memberId1,
                    @Param("memberId2") Long memberId2);

    /**
     * 특정 사용자가 보낸 친구 요청 목록 조회
     */
    @Query("SELECT fr FROM FriendRequest fr " +
                    "JOIN FETCH fr.receiver " +
                    "WHERE fr.requester.id = :requesterId " +
                    "AND fr.status = com.mini.buting.api.friend.domain.FriendRequest.RequestStatus.PENDING " +
                    "ORDER BY fr.createdAt DESC")
    List<FriendRequest> findPendingRequestsByRequester(@Param("requesterId") Long requesterId);

    /**
     * 특정 친구 요청 ID로 조회 (관계자만 접근 가능하도록 검증용)
     */
    @Query("SELECT fr FROM FriendRequest fr " +
                    "JOIN FETCH fr.requester " +
                    "JOIN FETCH fr.receiver " +
                    "WHERE fr.id = :requestId " +
                    "AND (fr.requester.id = :memberId OR fr.receiver.id = :memberId)")
    Optional<FriendRequest> findByIdAndMemberId(@Param("requestId") Long requestId,
                    @Param("memberId") Long memberId);
}
