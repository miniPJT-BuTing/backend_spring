package com.mini.buting.api.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "채팅 메시지 목록 응답(커서 기반)")
public record ChatMessagesResponse(
        @Schema(description = "채팅방 ID", example = "1463426641706356736")
        String roomId,

        @Schema(description = "메시지 리스트")
        List<ChatMessageResponse> messages,

        @Schema(description = "다음 조회 커서(없으면 null)", nullable = true, example = "5")
        Long nextCursor,

        @Schema(description = "추가 데이터 존재 여부", example = "false")
        boolean hasMore
) {}
