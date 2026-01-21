package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.chatroom.ChatRoom;
import com.mini.buting.api.chat.domain.chatroom.Vote;
import com.mini.buting.api.chat.domain.chatroom.VoteOption;
import com.mini.buting.api.chat.domain.payload.VotePayload;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.dto.request.VoteCreateRequest;
import com.mini.buting.api.chat.dto.request.VoteOptionRequest;
import com.mini.buting.api.chat.dto.response.VoteInfoResponse;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import com.mini.buting.api.chat.repository.VoteBallotRepository;
import com.mini.buting.api.chat.repository.VoteOptionRepository;
import com.mini.buting.api.chat.repository.VoteRepository;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoteService {

    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final SnowFlakeGenerator snowFlakeGenerator;
    private final ChatService chatService;
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteBallotRepository voteBallotRepository;
    private final TransactionTemplate transactionTemplate;

    public VoteInfoResponse createVote(String roomIdStr, Long senderId, VoteCreateRequest request) {
        Long roomId = Long.parseLong(roomIdStr);

        Vote vote = transactionTemplate.execute(status -> {
            chatRoomService.checkAuth(senderId, roomId);

            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.CHATROOM_NOT_EXISTS));

            Member member = memberRepository.findByIdWithDetails(senderId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

            Long id = snowFlakeGenerator.nextId();

            Vote voteCreate = Vote.create(id, room, member, null, request);
            voteRepository.save(voteCreate);

            createVoteOptions(request.options(), voteCreate);
            return voteCreate;
        });


        String messageId;
        try {
            messageId = chatService.sendMessage(
                    createVoteMessage(request, roomIdStr, vote.getId()),
                    senderId
            );
        } catch (Exception e) {
            log.error("vote message send failed. voteId={}", vote.getId(), e);
            return VoteInfoResponse.from(vote, voteOptionRepository.findByVote(vote));
        }

        transactionTemplate.executeWithoutResult(status -> {
            Vote managed = voteRepository.findById(vote.getId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.VOTE_NOT_FOUND));
            managed.attachMessage(messageId);
        });

        return VoteInfoResponse.from(vote, voteOptionRepository.findByVote(vote));
    }


    private void createVoteOptions(List<VoteOptionRequest> options, Vote vote) {
        List<VoteOption> rows = options.stream()
                .map(m -> VoteOption.create(
                                snowFlakeGenerator.nextId(),
                                vote,
                                m.text(),
                                m.order())
                        )
                .toList();

        voteOptionRepository.saveAll(rows);
    }

    private ChatMessageRequest createVoteMessage(VoteCreateRequest request, String roomIdStr, Long voteId){
        return new ChatMessageRequest(
                roomIdStr,
                MessageType.VOTE,
                new VotePayload(
                        voteId,
                        request.title()
                )
        );
    }
}
