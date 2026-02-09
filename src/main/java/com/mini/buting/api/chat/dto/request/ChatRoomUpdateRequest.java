package com.mini.buting.api.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 타이틀 변경 요청")
public record ChatRoomUpdateRequest(
        @Schema(description = "변경할 채팅방 제목", example = "새로운 채팅방 제목")
        String title
) {}
