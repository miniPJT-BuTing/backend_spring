package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatmessage.MessageUnreadRow;
import com.mini.buting.api.chat.domain.chatroom.ChatRoomMember;
import com.mini.buting.api.chat.domain.chatroom.ChatRoomMemberId;
import com.mini.buting.api.chat.dto.response.ChatMemberResponse;
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

    @Query("""
        select new com.mini.buting.api.chat.dto.response.ChatMemberResponse(
            m.id,
            m.nickname,
            m.gender,
            (m.id = cr.leader.id),
            u.name,
            c.name
        )
        from ChatRoomMember crm
            join crm.member m
            join crm.chatRoom cr
            left join m.universityDomain ud
            left join ud.university u
            left join m.college c
        where crm.chatRoom.roomId = :roomId
    """)
    List<ChatMemberResponse> findChatMembersByRoomId(@Param("roomId") Long roomId);

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

    @Query(value = """
        SELECT s.message_seq AS messageSeq,
               SUM(CASE WHEN crm.last_read_seq < s.message_seq THEN 1 ELSE 0 END) AS unreadCount
        FROM chat_room_member crm
        JOIN JSON_TABLE(:seqJson, '$[*]' COLUMNS(message_seq BIGINT PATH '$')) s
        WHERE crm.room_id = :roomId
        GROUP BY s.message_seq
        """, nativeQuery = true)
    List<MessageUnreadRow> findUnreadCounts(@Param("roomId") Long roomId,
                                                  @Param("seqJson") String seqJson);
}
