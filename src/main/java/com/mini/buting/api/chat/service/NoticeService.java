package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.domain.chatroom.Notice;
import com.mini.buting.api.chat.domain.payload.NoticePayload;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.dto.request.NoticeUpsertRequest;
import com.mini.buting.api.chat.dto.response.NoticeResponse;
import com.mini.buting.api.chat.dto.response.NoticeViewResponse;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import com.mini.buting.api.chat.repository.NoticeRepository;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;
    private final NoticeRepository noticeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public NoticeResponse upsertNotice(String roomIdStr, Long senderId, NoticeUpsertRequest request) {

        Long roomId  = Long.parseLong(roomIdStr);

        chatRoomService.checkAuth(senderId, roomId);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHATROOM_NOT_EXISTS));


        Member member = memberRepository.findByIdWithDetails(senderId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        Notice notice = noticeRepository.findById(roomId).orElse(null);
        NoticeResponse response = null;
        if (notice == null) {
            notice = Notice.create(room, request, member);
            noticeRepository.save(notice);
            response = NoticeResponse.created(notice);
        } else {
            notice.update(request, member);
            response = NoticeResponse.updated(notice);
        }

        ChatMessageRequest message = createNoticeMessage(response);

        chatService.sendMessage(message, senderId);
        return response;
    }
    private ChatMessageRequest createNoticeMessage(NoticeResponse response){
        return new ChatMessageRequest(
                response.roomId(),
                MessageType.NOTICE,
                new NoticePayload(
                        response.place(),
                        response.meetAt(),
                        response.description(),
                        response.action()
                )
        );
    }

    public NoticeViewResponse getNotice(String roomIdStr, Long senderId) {

        Long roomId  = Long.parseLong(roomIdStr);

        chatRoomService.checkAuth(senderId, roomId);

        if(!noticeRepository.existsById(roomId))
            throw new BaseException(BaseResponseStatus.NOTICE_NOT_FOUND);

        return noticeRepository.findNoticeInfoById(roomId);
    }

    @Transactional
    public void delete(String roomIdStr, Long senderId) {
        Long roomId = Long.parseLong(roomIdStr);

        chatRoomService.checkAuth(senderId, roomId);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CHATROOM_NOT_EXISTS));
        chatRoomService.isLeader(senderId, room);

        int deleted = noticeRepository.deleteByRoomId(roomId);
        if (deleted == 0) {
            throw new BaseException(BaseResponseStatus.NOTICE_NOT_FOUND);
        }
    }
}
