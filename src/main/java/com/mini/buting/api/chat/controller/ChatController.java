package com.mini.buting.api.chat.controller;

import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.service.ChatRoomService;
import com.mini.buting.api.chat.service.ChatService;
import com.mini.buting.api.matchRequest.domain.MatchRequest;
import com.mini.buting.api.matchRequest.repository.MatchRequestRepository;
import com.mini.buting.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final MatchRequestRepository matchRequestRepository;

    // 메시지 전송 - senderId는 수정 예정
    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(
            @Payload ChatMessageRequest message,
            @Header("senderId") Long senderId
    ) {
        chatService.sendMessage(message, senderId);
    }

    // 최근 메시지 조회 + 채팅방 정보 (제목, 인원, 참여한 멤버 ..., 공지)

    // 이전 메시지 조회

    // 채팅방 생성 (테스트용. 실서비스에서는 api 없음)
    @PostMapping
    public String createChatroom(@RequestParam Long matchRequestId) throws IllegalAccessException {
        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId).orElseThrow(() -> new IllegalAccessException());
        ChatRoom room = chatRoomService.createRoom(matchRequest);
        return room.getRoomId() + " " + room.getTitle();
    }


    // 채팅방 목록 조회

    // 채팅방 타이틀 변경

    // 공지 등록

    // 공지 조회

    // 메시지 좋아요

    // 투표 등록

    // 투표 조회

    // 투표 하기

}
