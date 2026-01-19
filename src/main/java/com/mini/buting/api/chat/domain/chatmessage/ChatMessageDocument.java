package com.mini.buting.api.chat.domain.chatmessage;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.MessageStatus;
import com.mini.buting.api.chat.domain.payload.Payload;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@CompoundIndexes({
        @CompoundIndex(name = "idx_room_seq", def = "{'roomId': 1, 'messageSeq': -1}")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDocument {

    @Id
    private String id;

    private Long roomId;
    private Long messageSeq;

    private MessageType type;
    private Long senderId;
    private String content;
    private Payload payload;
    private LocalDateTime createdAt;
    private MessageStatus messageStatus;
}
