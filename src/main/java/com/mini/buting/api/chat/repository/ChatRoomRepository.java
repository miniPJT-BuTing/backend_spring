package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.dto.response.ChatRoomSummaryResponse;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.team.domain.Team;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByMaleTeamAndFemaleTeam(Team maleTeam, Team femaleTeam);
        @Query("""
        select new com.mini.buting.api.chat.dto.response.ChatRoomSummaryResponse(
            cast(cr.roomId as string),
            cr.title,
            cr.memberCount,
            cr.createdAt,
            cr.lastMessage.seq
        )
        from ChatRoom cr
        where cr.roomId = :roomId
    """)
        ChatRoomSummaryResponse findSummaryByRoomId(@Param("roomId") Long roomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE chat_room
           SET last_message_type = :type,
               last_message_preview = :preview,
               last_message_sent_at = :sentAt,
               last_message_seq = :seq
         WHERE room_id = :roomId
           AND (last_message_seq IS NULL OR last_message_seq < :seq)
    """, nativeQuery = true)
    int updateLastMessageIfNewer(
            @Param("roomId") Long roomId,
            @Param("type") String type,
            @Param("preview") String preview,
            @Param("sentAt") LocalDateTime sentAt,
            @Param("seq") Long seq
    );
}
