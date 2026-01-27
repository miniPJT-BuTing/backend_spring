package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.chatroom.VoteOption;

import java.util.List;

public record VoteOptionResponse(
        String optionId,
        String text,
        Integer order,
        Integer count,
        List<VoterResponse> voters
) {

    public static VoteOptionResponse from(VoteOption voteOption){
        return new VoteOptionResponse(
                voteOption.getOptionId().toString(),
                voteOption.getOptionText(),
                voteOption.getOptionOrder(),
                0,
                null
        );
    }

    public static List<VoteOptionResponse> fromOptions(List<VoteOption> options){

        return options.stream()
                .map(VoteOptionResponse::from)
                .toList();
    }

}
