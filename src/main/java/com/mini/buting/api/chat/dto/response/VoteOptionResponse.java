package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.chatroom.VoteOption;

import java.util.List;

public record VoteOptionResponse(
        String text,
        Integer order,
        Integer count
) {

    public static VoteOptionResponse from(VoteOption voteOption){
        return new VoteOptionResponse(
                voteOption.getOptionText(),
                voteOption.getOptionOrder(),
                0
        );
    }

    public static List<VoteOptionResponse> from(List<VoteOption> options){

        return options.stream()
                .map(VoteOptionResponse::from)
                .toList();
    }
}
