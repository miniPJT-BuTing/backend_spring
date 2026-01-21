package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import com.mini.buting.api.chat.domain.payload.NoticePayload;
import com.mini.buting.api.chat.domain.payload.TextPayload;
import com.mini.buting.api.chat.domain.payload.WelcomePayload;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.dto.response.NoticeAction;
import com.mini.buting.api.chat.repository.ChatRoomMemberRepository;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final RedisChatService redisChatService;
    private final MongoChatService mongoChatService;
    private final ChatPublisher chatPublisher;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomListService chatRoomListService;

    // 메시지 전송
    @Transactional
    public void sendMessage(ChatMessageRequest message, Long senderId) {

        Long roomId = Long.parseLong(message.roomId());

        // 1) 채팅방 존재
        if (!chatRoomRepository.existsById(roomId)) {
            throw new BaseException(BaseResponseStatus.CHATROOM_NOT_EXISTS);
        }

        // 2) 권한
        if (!chatRoomMemberRepository.existsByIdRoomIdAndIdMemberId(roomId, senderId)) {
            throw new BaseException(BaseResponseStatus.NOT_CHATROOM_MEMBER);
        }

        // 3) seq 발급 (Redis INCR)
        long seq = redisChatService.nextSeq(roomId);

        String content = makePreview(message);

        // 4) Mongo 저장
        ChatMessageDocument messageDocument = mongoChatService.saveMessage(message, senderId, seq, content);

        // 5) MySQL 요약(lastMessage) 갱신
        chatRoomRepository.updateLastMessageIfNewer(
                roomId,
                messageDocument.getType().name(),
                content,
                messageDocument.getCreatedAt(),
                seq
        );

        // 6) Publish
        try {
            chatPublisher.publish(messageDocument);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.MESSAGE_PUBLISH_FAILED);
        }

        // 7) Redis cache
        try {
            redisChatService.updateCache(messageDocument);
        } catch (Exception e) {
            log.warn("chatroom redis caching failed : room[" + message.roomId() + "] seq[" + seq + "]");
        }

        // 채팅방 목록용 이벤트
        chatRoomListService.notifyRoomUpdated(roomId);

    }

    private String makePreview(ChatMessageRequest message) {
        switch (message.type()) {
            case VOTE:
                return "투표가 등록되었습니다.";

            case IMAGE:
                return "사진을 보냈습니다.";

            case NOTICE:
                if(message.payload() instanceof NoticePayload){
                    if (((NoticePayload) message.payload()).action().equals(NoticeAction.CREATED))
                        return "공지가 등록되었습니다.";
                    else return "공지가 수정되었습니다.";
                }
                else{
                    throw new BaseException(BaseResponseStatus.MESSAGE_PUBLISH_FAILED);
                }
            case WELCOME:
                return "매칭에 성공했어요! 대화를 나눠보세요";
            default:
                if(message.payload() instanceof TextPayload){
                    TextPayload payload = (TextPayload) message.payload();
                    String text = payload.text();
                    return text.length() <= 20 ? text : text.substring(0, 20);
                }
        }
        return null;
    }

}
