package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.chatroom.Vote;
import com.mini.buting.api.chat.domain.chatroom.VoteOption;
import com.mini.buting.api.chat.domain.chatroom.VoteStatus;

import java.util.ArrayList;
import java.util.List;

public record VoteInfoResponse(
        String voteId,
        String title,
        String description,
        boolean multiple,
        boolean anonymous,
        VoteStatus status,
        List<VoteOptionResponse> options,
        List<Long> mySelections,
        String nickname,
        Long creator
) {

    public static VoteInfoResponse from(Vote vote, List<VoteOption> options){

        List<VoteOptionResponse> optionResponses = VoteOptionResponse.fromOptions(options);

        return new VoteInfoResponse(
                vote.getId().toString(),
                vote.getTitle(),
                vote.getDescription(),
                vote.isMultiple(),
                vote.isAnonymous(),
                vote.getStatus(),
                optionResponses,
                new ArrayList<>(),
                vote.getCreatedBy().getNickname(),
                vote.getCreatedBy().getId()
        );
    }

    public static VoteInfoResponse of(Vote vote, List<VoteOptionResponse> optionCounts, List<Long> mySelections) {

        return new VoteInfoResponse(
                vote.getId().toString(),
                vote.getTitle(),
                vote.getDescription(),
                vote.isMultiple(),
                vote.isAnonymous(),
                vote.getStatus(),
                optionCounts,
                mySelections,
                vote.getCreatedBy().getNickname(),
                vote.getCreatedBy().getId()
        );
    }
}
