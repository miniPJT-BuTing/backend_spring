package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.MessageStatus;
import com.mini.buting.api.chat.domain.payload.Payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String messageId;
    private Long roomId;
    private Long messageSeq;
    private MessageType type;
    private Long senderId;
    private String content;
    private Payload payload;
    private LocalDateTime createdAt;
    private MessageStatus messageStatus;

}
