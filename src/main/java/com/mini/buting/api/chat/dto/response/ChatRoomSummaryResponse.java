package com.mini.buting.api.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 요약 정보")
public record ChatRoomSummaryResponse(
        @Schema(description = "채팅방 ID", type = "string", example = "1463426641706356736")
        String roomId,

        @Schema(description = "채팅방 제목", example = "채팅남팀")
        String title,

        @Schema(description = "현재 인원수", example = "4")
        Integer memberCount,

        @Schema(description = "채팅방 생성 시각", example = "2026-01-21T15:55:00.536767")
        LocalDateTime createdAt,

        @Schema(description = "마지막 메시지 시퀀스", example = "5", nullable = true)
        Long lastMessageSeq
) {}
