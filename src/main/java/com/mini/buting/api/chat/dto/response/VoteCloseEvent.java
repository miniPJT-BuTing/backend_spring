package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.chatroom.Vote;

import java.util.List;

public record VoteCloseEvent(
        Vote vote,
        List<String> winners
) {}
