package com.mini.buting.api.chat.service;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.chatroom.*;
import com.mini.buting.api.chat.domain.payload.VotePayload;
import com.mini.buting.api.chat.dto.request.ChatMessageRequest;
import com.mini.buting.api.chat.dto.request.VoteBallotCreateRequest;
import com.mini.buting.api.chat.dto.request.VoteCreateRequest;
import com.mini.buting.api.chat.dto.request.VoteOptionRequest;
import com.mini.buting.api.chat.dto.response.*;
import com.mini.buting.api.chat.repository.ChatRoomRepository;
import com.mini.buting.api.chat.repository.VoteBallotRepository;
import com.mini.buting.api.chat.repository.VoteOptionRepository;
import com.mini.buting.api.chat.repository.VoteRepository;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public VoteInfoResponse vote(String roomIdStr, String voteIdStr, Long senderId, VoteBallotCreateRequest selected){

        Long roomId = Long.parseLong(roomIdStr);
        Long voteId = Long.parseLong(voteIdStr);

        Vote vote = getVote(roomId, voteId, senderId);

        if(!vote.getStatus().equals(VoteStatus.OPEN)){
            throw new BaseException(BaseResponseStatus.VOTE_NOT_AVAILABLE);
        }

        List<Long> options = selected.options();

        if (options == null || options.isEmpty()) {
            throw new BaseException(BaseResponseStatus.VOTE_OPTION_REQUIRED);
        }

        Set<Long> uniqueOptionIds = new LinkedHashSet<>(options);

        Set<Long> validOptionSet = new HashSet<>(voteOptionRepository.findOptionIdsByVoteId(voteId));
        if (!validOptionSet.containsAll(uniqueOptionIds)) {
            throw new BaseException(BaseResponseStatus.INVALID_VOTE_OPTION);
        }

        if(!vote.isMultiple() && uniqueOptionIds.size() > 1){
            throw new BaseException(BaseResponseStatus.VOTE_NOT_MULTIPLE);
        }

        voteBallotRepository.deleteByVoteIdAndMemberId(voteId, senderId);

        List<VoteBallot> ballots = uniqueOptionIds.stream()
                .map(optionId -> VoteBallot.create(voteId, optionId, senderId))
                .toList();

        voteBallotRepository.saveAll(ballots);

        return getVoteInfo(roomIdStr, voteIdStr, senderId);
    }

    public Vote getVote(Long roomId, Long voteId, Long senderId){
        chatRoomService.checkAuth(senderId, roomId);

        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.VOTE_NOT_FOUND));

        if(!vote.getChatRoom().getRoomId().equals(roomId))
            throw new BaseException(BaseResponseStatus.CHAT_VOTE_UNMATCHED);

        return vote;
    }

    public VoteInfoResponse getVoteInfo(String roomIdStr, String voteIdStr, Long senderId) {
        Long roomId = Long.parseLong(roomIdStr);
        Long voteId = Long.parseLong(voteIdStr);

        Vote vote = getVote(roomId, voteId, senderId);

        // 3) 옵션별 투표 수
        List<VoteOptionCountProjection> optionCounts =
                voteOptionRepository.findOptionCounts(voteId);

        Map<Long, List<VoterResponse>> votersByOption = new HashMap<>();

        boolean canSeeVoters = !vote.isAnonymous();

        if (canSeeVoters) {
            Map<Long, List<VoterResponse>> temp =
                    voteBallotRepository.findVotersByVoteId(voteId).stream()
                            .collect(Collectors.groupingBy(
                                    VoteOptionVoterProjection::getOptionId,
                                    Collectors.mapping(
                                            p -> new VoterResponse(p.getMemberId(), p.getNickname()),
                                            Collectors.toList()
                                    )
                            ));
            votersByOption.putAll(temp);
        }

        // 4) 내 선택
        List<Long> mySelections =
                voteBallotRepository.findOptionIdsByVoteAndMember(voteId, senderId);

        List<VoteOptionResponse> options =
                optionCounts.stream()
                        .map(p -> new VoteOptionResponse(
                                p.getOptionId().toString(),
                                p.getText(),
                                p.getOrder(),
                                p.getCount(),
                                canSeeVoters
                                        ? votersByOption.getOrDefault(p.getOptionId(), List.of())
                                        : null
                        ))
                        .toList();

        // 5) 응답 조립
        return VoteInfoResponse.of(vote, options, mySelections);

    }

}
