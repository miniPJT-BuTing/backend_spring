package com.mini.buting.api.chat.domain.chatmessage;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.MessageStatus;
import com.mini.buting.api.chat.domain.payload.Payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedChatMessage {
    private String messageId;
    private Long roomId;
    private Long messageSeq;
    private MessageType type;
    private Long senderId;
    private String content;
    private Payload payload;
    private LocalDateTime createdAt;
    private MessageStatus messageStatus;

    public static CachedChatMessage of(ChatMessageDocument message) {

        // 타입별 payload 전처리?

        return CachedChatMessage.builder()
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
}
