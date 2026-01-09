package com.mini.buting.api.chat.controller;

import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatMessageProducer;

    // 메시지 전송
    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessageRequest message) {

        // 추후 변경 예정
        Long senderId =  1L;
        chatMessageProducer.sendMessage(message, senderId);

    }

    // 최근 메시지 조회

    // 이전 메시지 조회

    // 채팅방 생성 (테스트용. 실서비스에서는 api 없음)

    // 채팅방 목록 조회

    // 채팅방 타이틀 변경

    // 공지 등록

    // 공지 조회

    // 메시지 좋아요

    // 투표 등록

    // 투표 조회

    // 투표 하기

}
