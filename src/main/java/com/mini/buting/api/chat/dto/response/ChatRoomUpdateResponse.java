package com.mini.buting.api.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 타이틀 변경 응답")
public record ChatRoomUpdateResponse(
        @Schema(description = "채팅방 ID", example = "1463426641706356736")
        String roomId,

        @Schema(description = "변경된 채팅방 제목", example = "새로운 채팅방 제목")
        String title
) {}
