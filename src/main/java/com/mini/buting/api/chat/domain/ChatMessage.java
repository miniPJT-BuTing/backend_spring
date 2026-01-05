package com.mini.buting.api.chat.domain;

import lombok.*;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
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
