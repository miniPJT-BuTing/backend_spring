package com.mini.buting.api.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import com.mini.buting.api.chat.domain.chatmessage.MessageUnreadRow;
import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.domain.chatroom.ChatRoomMember;
import com.mini.buting.api.chat.domain.payload.WelcomePayload;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.dto.response.*;
import com.mini.buting.api.chat.repository.ChatRoomMemberRepository;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import com.mini.buting.api.matchRequest.domain.MatchRequest;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.team.domain.Team;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SnowFlakeGenerator snowFlakeGenerator;
    private final RedisChatService redisChatService;
    private final MongoChatService mongoChatService;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final ChatRoomListService chatRoomListService;

    // 채팅방 생성
    public ChatRoom createRoom(MatchRequest match) {

        match.validateAcceptedStatus();

        Team maleTeam;
        Team femaleTeam;

        Team requestTeam = match.getRequestTeam();
        if(requestTeam.getGender() == Team.Gender.MALE){
            maleTeam = requestTeam;
            femaleTeam = match.getTargetTeam();
        }
        else{
            maleTeam = match.getTargetTeam();
            femaleTeam = requestTeam;
        }

        if (chatRoomRepository.existsByMaleTeamAndFemaleTeam(maleTeam, femaleTeam)) {
            throw new BaseException(BaseResponseStatus.CHATROOM_ALREADY_EXISTS);
        }

        return createRoom(match.getRequestTeam().getTitle(), maleTeam, femaleTeam, match.getRequestTeam().getLeader());
    }

    public void sendWelcomeMessage(Long roomId, Long leaderId) {

        ChatMessageRequest message = new ChatMessageRequest(
                String.valueOf(roomId),
                MessageType.WELCOME,
                new WelcomePayload("매칭에 성공했어요! 대화를 나눠보세요.")
        );

        chatService.sendMessage(message, leaderId);
    }

    private ChatRoom createRoom(String title, Team maleTeam, Team femaleTeam, Member leader) {
        ChatRoom room = ChatRoom.builder()
                .roomId(snowFlakeGenerator.nextId())
                .title(title)
                .maleTeam(maleTeam)
                .femaleTeam(femaleTeam)
                .leader(leader)
                .memberCount(maleTeam.getTeamSize().getSize() * 2)
                .lastMessage(null)
                .build();


        ChatRoom saved = chatRoomRepository.saveAndFlush(room);

        createMembers(saved, maleTeam.getMembers());
        createMembers(saved, femaleTeam.getMembers());

        chatRoomListService.notifyRoomUpdated(saved.getRoomId());

        return saved;
    }

    // 채팅방 생성 시 멤버랑 연결
    public void createMembers(ChatRoom chatRoom, List<Member> participants) {
        // 중복 방지(이미 만들어진 경우)도 하고 싶으면 exists 체크
        List<ChatRoomMember> rows = participants.stream()
                .map(m -> ChatRoomMember.builder()
                        .chatRoom(chatRoom)
                        .member(m)
                        .lastReadSeq(0L)
                        .build())
                .toList();

        chatRoomMemberRepository.saveAll(rows);
    }

    //채팅방 입장
    public ChatRoomInfoResponse enterChatroom(String roomIdStr, Long senderId) {

        Long roomId = Long.parseLong(roomIdStr);

        checkAuth(senderId, roomId);

        // 채팅방 정보 조회
        ChatRoomSummaryResponse roomInfo = chatRoomRepository.findSummaryByRoomId(roomId);

        // 채팅방 멤버 조회
        List<ChatMemberResponse> allByIdRoomId = chatRoomMemberRepository.findChatMembersByRoomId(roomId);

        markAsRead(roomId, senderId, roomInfo.lastMessageSeq());

        ChatMessagesResponse messages = getMessages(roomIdStr, senderId, null);

        return new ChatRoomInfoResponse(roomInfo, allByIdRoomId, messages);
    }

    private List<ChatMessageResponse> getRecentMessages(Long roomId) {
        // 최근 메시지 조회
        List<ChatMessageResponse> messages = redisChatService.getRecentMessages(roomId);
        if (messages.isEmpty()) {
            List<ChatMessageDocument> fromMongo = mongoChatService.findRecentMessages(roomId, 50);

            redisChatService.fillCacheFromMongo(roomId, fromMongo);

            messages = fromMongo.stream()
                    .map(ChatMessageResponse::of)
                    .toList();
        }
        return messages;
    }

    public ChatMessagesResponse getMessages(String roomIdStr, Long senderId, Long beforeSeq) {

        Long roomId = Long.parseLong(roomIdStr);

        checkAuth(senderId, roomId);

        List<ChatMessageResponse> messages = null;

        // 입장의 경우
        if(beforeSeq == null){
            messages = getRecentMessages(roomId);
        }
        else{

            List<ChatMessageDocument> fromMongo = mongoChatService.findMessages(roomId, beforeSeq);

            messages = fromMongo.stream()
                    .map(ChatMessageResponse::of)
                    .toList();
        }

        if (messages.isEmpty()) {
            return new ChatMessagesResponse(roomIdStr, List.of(), null, false);
        }

        try{

            List<Long> seqs = messages.stream()
                    .map(ChatMessageResponse::getMessageSeq)
                    .toList();

            String seqJson = objectMapper.writeValueAsString(seqs);

            Map<Long, Long> unreadMap = chatRoomMemberRepository.findUnreadCounts(roomId, seqJson).stream()
                    .collect(Collectors.toMap(MessageUnreadRow::getMessageSeq, MessageUnreadRow::getUnreadCount));

            messages.forEach(msg -> msg.setUnreadCount(unreadMap.getOrDefault(msg.getMessageSeq(), 0L)));

        } catch (JsonProcessingException e) {
            throw new BaseException(BaseResponseStatus.MESSAGE_LOAD_FAIL);
        }

        boolean hasMore = (messages.size() == 50);
        Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessageSeq();

        return new ChatMessagesResponse(roomIdStr, messages, nextCursor, hasMore);

    }

    private void checkAuth(Long senderId, Long roomId) {
        // 1) 채팅방 존재
        if (!chatRoomRepository.existsById(roomId)) {
            throw new BaseException(BaseResponseStatus.CHATROOM_NOT_EXISTS);
        }

        // 2) 권한
        if (!chatRoomMemberRepository.existsByIdRoomIdAndIdMemberId(roomId, senderId)) {
            throw new BaseException(BaseResponseStatus.NOT_CHATROOM_MEMBER);
        }
    }

    // 채팅방 목록 읽음 처리
    public void markAsRead(Long roomId, Long userId, Long newSeq) {
        if (newSeq == null) return;
        int updated = chatRoomMemberRepository.updateLastReadSeqMax(roomId, userId, newSeq);

        // 혹시 데이터가 없으면 예외
        if (updated == 0) {
            throw new BaseException(BaseResponseStatus.NOT_CHATROOM_MEMBER);
        }

        chatRoomListService.notifyRoomUpdated(roomId);
    }

}
