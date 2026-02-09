package com.mini.buting.api.chat.dto.response;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 입장 응답")
public record ChatRoomInfoResponse(
        @Schema(description = "채팅방 요약 정보")
        ChatRoomSummaryResponse roomInfo,

        @Schema(description = "공지 요약 정보 (없으면 null)", nullable = true)
        NoticeSummaryInfo noticeInfo,

        @Schema(description = "채팅방 참여 멤버 목록")
        List<ChatMemberResponse> memberInfo,

        @Schema(description = "초기 메시지 목록/페이징 정보")
        ChatMessagesResponse messages
) {}
