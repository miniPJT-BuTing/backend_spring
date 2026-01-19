package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.dto.response.ChatRoomListUpdateEvent;
import com.mini.buting.api.chat.dto.response.ChatRoomResponse;
import com.mini.buting.api.chat.dto.response.RoomMemberReadProjection;
import com.mini.buting.api.chat.repository.ChatRoomMemberRepository;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomListService {
    private final ChatPublisher chatPublisher;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 채팅방 목록 조회
    public List<ChatRoomResponse> getChatRooms(Long senderId) {
        return chatRoomMemberRepository.findChatRoomsForMember(senderId);
    }

    @Transactional(readOnly = true)
    public void notifyRoomUpdated(Long roomId) {

        var room = chatRoomRepository.findById(roomId)
                .orElseThrow();

        Long lastSeq = (room.getLastMessage() == null) ? null : room.getLastMessage().getSeq();

        List<RoomMemberReadProjection> members = chatRoomMemberRepository.findMemberReadsByRoomId(roomId);

        for (var m : members) {
            long unread = 0L;
            if (lastSeq != null) {
                unread = Math.max(0L, lastSeq - m.getLastReadSeq());
            }

            ChatRoomListUpdateEvent event = new ChatRoomListUpdateEvent(
                    String.valueOf(room.getRoomId()),
                    room.getTitle(),
                    room.getMemberCount(),
                    room.getLastMessage() == null ? room.getCreatedAt() : room.getLastMessage().getSentAt(),
                    room.getLastMessage() == null ? null : room.getLastMessage().getPreview(),
                    room.getLastMessage() == null ? null : room.getLastMessage().getSentAt(),
                    lastSeq,
                    unread
            );

            chatPublisher.publish(m, event);

        }
    }
}
