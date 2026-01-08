package com.mini.buting.api.chat.domain.chatmessage;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.MessageStatus;
import com.mini.buting.api.chat.domain.payload.Payload;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDocument {

    @Id
    private String id;

    @Indexed
    private Long roomId;

    @Indexed
    private Long messageSeq;

    private MessageType type;
    private Long senderId;
    private String content;
    private Payload payload;
    private LocalDateTime createdAt;
    private MessageStatus messageStatus;
}
