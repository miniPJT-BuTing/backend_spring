package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.dto.response.ChatReadEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatReadNotifier {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastRead(Long roomId, Long readerId, Long lastReadSeq) {
        messagingTemplate.convertAndSend(
                "/topic/chat.read." + roomId,
                new ChatReadEvent(String.valueOf(roomId), readerId, lastReadSeq)
        );
    }
}
