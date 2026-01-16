package com.mini.buting.api.chat.dto.response;

import java.time.LocalDateTime;

public record ChatRoomResponse (
        String roomId,
        String title,
        Integer memberCount,
        LocalDateTime createdAt,
        LastMessageResponse lastMessage,
        Integer unreadCount
){

}
