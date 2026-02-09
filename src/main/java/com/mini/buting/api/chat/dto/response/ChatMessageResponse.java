package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.MessageStatus;
import com.mini.buting.api.chat.domain.chatmessage.CachedChatMessage;
import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import com.mini.buting.api.chat.domain.payload.Payload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 메시지")
public class ChatMessageResponse {

    @Schema(description = "메시지 ID", example = "msg_01HXYZ...")
    private String messageId;

    @Schema(description = "채팅방 ID", example = "1463426641706356736")
    private Long roomId;

    @Schema(description = "메시지 시퀀스", example = "5")
    private Long messageSeq;

    @Schema(description = "메시지 타입", example = "TEXT")
    private MessageType type;

    @Schema(description = "발신자 ID", example = "2")
    private Long senderId;

    @Schema(description = "텍스트 내용", example = "안녕하세요")
    private String content;

    @Schema(description = "확장 payload(투표/공지/시스템 등)", nullable = true)
    private Payload payload;

    @Schema(description = "생성 시각", example = "2026-01-21T16:01:02.123")
    private LocalDateTime createdAt;

    @Schema(description = "메시지 상태", example = "NORMAL")
    private MessageStatus messageStatus;

    @Schema(description = "안 읽은 사람 수", example = "3")
    private Long unreadCount;

    public static ChatMessageResponse of(CachedChatMessage message) {

        // 타입별 payload 전처리?

        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .roomId(message.getRoomId())
                .messageSeq(message.getMessageSeq())
                .type(message.getType())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .payload(message.getPayload())
                .createdAt(message.getCreatedAt())
                .messageStatus(message.getMessageStatus())
                .build();
    }


    public static ChatMessageResponse of(ChatMessageDocument message) {

        // 타입별 payload 전처리?

        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getRoomId())
                .messageSeq(message.getMessageSeq())
                .type(message.getType())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .payload(message.getPayload())
                .createdAt(message.getCreatedAt())
                .messageStatus(message.getMessageStatus())
                .build();
    }

    public void setUnreadCount(Long orDefault) {
        this.unreadCount = orDefault;
    }
}
