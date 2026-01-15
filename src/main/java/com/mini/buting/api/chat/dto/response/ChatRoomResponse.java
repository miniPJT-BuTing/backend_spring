package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.chatmessage.CachedChatMessage;

import java.util.List;

public record ChatRoomResponse(
        ChatRoomSummaryResponse roomInfo,
        List<ChatMemberResponse> memberInfo,
        List<CachedChatMessage> messages,
        Long nextCursor,
        boolean hasMore
) {
}

