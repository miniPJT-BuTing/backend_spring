package com.mini.buting.api.chat.domain;

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
    private Status status;
}
