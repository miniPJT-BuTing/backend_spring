package com.mini.buting.api.chat.controller;

import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.dto.request.ChatReadRequest;
import com.mini.buting.api.chat.dto.request.ChatRoomUpdateRequest;
import com.mini.buting.api.chat.dto.response.ChatMessagesResponse;
import com.mini.buting.api.chat.dto.response.ChatRoomInfoResponse;
import com.mini.buting.api.chat.dto.response.ChatRoomResponse;
import com.mini.buting.api.chat.dto.response.ChatRoomUpdateResponse;
import com.mini.buting.api.chat.service.ChatReadNotifier;
import com.mini.buting.api.chat.service.ChatRoomListService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final MatchRequestRepository matchRequestRepository;
    private final ChatReadNotifier chatReadNotifier;
    private final ChatRoomListService chatRoomListService;

    // 메시지 전송 - senderId는 수정 예정
    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(
            @Payload ChatMessageRequest message,
            @Header("senderId") Long senderId
    ) {
        chatService.sendMessage(message, senderId);
    }

    @MessageMapping("chat.read.{roomId}")
    public void markRead(
            @DestinationVariable String roomId,
            ChatReadRequest req,
            @Header("senderId") Long senderId
    ){
        chatRoomService.markAsRead(Long.parseLong(roomId), senderId, req.lastReadSeq());
        chatReadNotifier.broadcastRead(Long.parseLong(roomId), senderId, req.lastReadSeq());
    }

    // 채팅방 정보 (제목, 인원, 참여한 멤버 ..., 공지)
    // 공지 기능 구현 후에 추가하기!
    @GetMapping("/{roomId}")
    public BaseResponse<ChatRoomInfoResponse> enterChatroom(
            @PathVariable String roomId,
            @RequestHeader("senderId") Long senderId
    ){
        ChatRoomInfoResponse roomInfo = chatRoomService.enterChatroom(roomId, senderId);

        return BaseResponse.onSuccess(roomInfo);
    }

    // 이전 메시지 조회
    @GetMapping("/{roomId}/messages")
    //beforeSeq=12345
    public BaseResponse<ChatMessagesResponse> getMessages(
            @PathVariable String roomId,
            @RequestHeader("senderId") Long senderId,
            @RequestParam Long beforeSeq
    ){
        ChatMessagesResponse messages = chatRoomService.getMessages(roomId, senderId, beforeSeq);

        return BaseResponse.onSuccess(messages);
    }

    // 채팅방 생성 (테스트용. 실서비스에서는 api 없음)
    @PostMapping
    public String createChatroom(@RequestParam Long matchRequestId) throws IllegalAccessException {
        MatchRequest matchRequest = matchRequestRepository.findById(matchRequestId).orElseThrow(() -> new IllegalAccessException());
        ChatRoom room = chatRoomService.createRoom(matchRequest);
        chatRoomService.sendWelcomeMessage(room.getRoomId(), room.getLeader().getId());
        return room.getRoomId() + " " + room.getTitle();
    }


    // 채팅방 목록 조회
    @GetMapping
    public BaseResponse<List<ChatRoomResponse>> getChatRooms(
            @RequestHeader("senderId") Long senderId
    ){
        List<ChatRoomResponse> chatRooms = chatRoomListService.getChatRooms(senderId);
        return BaseResponse.onSuccess(chatRooms);
    }

    // 채팅방 타이틀 변경
    @PutMapping("/{roomId}")
    public BaseResponse<ChatRoomUpdateResponse> updateChatRoomTitle(
            @PathVariable String roomId,
            @RequestHeader("senderId") Long senderId,
            @RequestBody ChatRoomUpdateRequest chatRoomUpdateRequest
    ){
        ChatRoomUpdateResponse updateRoom = chatRoomService.updateChatRoomTitle(roomId, senderId, chatRoomUpdateRequest);
        return BaseResponse.onSuccess(updateRoom);
    }

    // 공지 등록

    // 공지 조회

    // 메시지 좋아요

    // 투표 등록

    // 투표 조회

    // 투표 하기

}
