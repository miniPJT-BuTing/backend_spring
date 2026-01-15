package com.mini.buting.api.chat.dto.response;

import java.util.List;

public record ChatMessagesResponse(
        String roomId,
        List<ChatMessageResponse> messages,
        Long nextCursor,
        boolean hasMore
) {}
