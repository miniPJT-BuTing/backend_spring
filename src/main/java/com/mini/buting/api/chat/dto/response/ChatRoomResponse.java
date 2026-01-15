package com.mini.buting.api.chat.dto.response;

import java.util.List;

public record ChatRoomResponse(
        ChatRoomSummaryResponse roomInfo,
        List<ChatMemberResponse> memberInfo,
        ChatMessagesResponse messages
) {
}

