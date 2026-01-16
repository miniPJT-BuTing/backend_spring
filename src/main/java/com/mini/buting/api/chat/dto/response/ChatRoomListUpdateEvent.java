package com.mini.buting.api.chat.dto.response;

import java.time.LocalDateTime;

public record ChatRoomListUpdateEvent(
        String roomId,
        String title,
        Integer memberCount,
        LocalDateTime updatedAt,
        String lastPreview,
        LocalDateTime lastSentAt,
        Long lastSeq,
        Long unreadCount
) {}
