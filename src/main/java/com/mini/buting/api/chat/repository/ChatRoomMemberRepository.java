package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.ChatRoomMember;
import com.mini.buting.api.chat.domain.chatroom.ChatRoomMemberId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {

    Optional<ChatRoomMember> findByIdRoomIdAndIdMemberId(Long roomId, Long memberId);

    boolean existsByIdRoomIdAndIdMemberId(Long roomId, Long memberId);

    List<ChatRoomMember> findAllByIdRoomId(Long roomId);

    List<ChatRoomMember> findAllByIdMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
            UPDATE chat_room_member
               SET last_read_seq = GREATEST(last_read_seq, :newSeq)
             WHERE room_id = :roomId
               AND member_id = :memberId
        """,
            nativeQuery = true
    )
    int updateLastReadSeqMax(@Param("roomId") Long roomId,
                             @Param("memberId") Long memberId,
                             @Param("newSeq") Long newSeq);
}
