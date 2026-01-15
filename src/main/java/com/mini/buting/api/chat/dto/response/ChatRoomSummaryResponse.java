package com.mini.buting.api.chat.dto.response;

import java.time.LocalDateTime;

public record ChatRoomSummaryResponse(
        String roomId,
        String title,
        Integer memberCount,
        LocalDateTime createdAt,
        Long lastMessageSeq
) {}
