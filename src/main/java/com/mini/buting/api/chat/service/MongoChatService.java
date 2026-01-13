package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.MessageStatus;
import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MongoChatService {

    private final ChatMessageRepository chatMessageRepository;
    //메시지 저장
    public ChatMessageDocument saveMessage(ChatMessageRequest message, Long senderId, long seq, String content) {
        ChatMessageDocument messageDocument = ChatMessageDocument.builder()
                .roomId(Long.parseLong(message.roomId()))
                .messageSeq(seq)
                .type(message.type())
                .senderId(senderId)
                .content(content)
                .payload(message.payload())
                .createdAt(LocalDateTime.now())
                .messageStatus(MessageStatus.NORMAL)
                .build();

        chatMessageRepository.save(messageDocument);
        return messageDocument;
    }

    //메시지 불러오기 - 페이징

}
