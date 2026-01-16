package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.dto.response.ChatRoomListUpdateEvent;
import com.mini.buting.api.chat.dto.response.RoomMemberReadProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatPublisher {

    @Value("${rabbitmq.chat-exchange.name}")
    private String CHAT_EXCHANGE_NAME;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public void publish(ChatMessageDocument message) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "chat.room." + message.getRoomId(), message);
    }

    public void publish(RoomMemberReadProjection member, ChatRoomListUpdateEvent evt) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(member.getMemberId()),
                "/queue/chatroom.list",
                evt
        );
    }
}
