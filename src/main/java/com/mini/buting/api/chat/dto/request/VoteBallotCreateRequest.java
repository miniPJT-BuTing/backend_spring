package com.mini.buting.api.chat.dto.request;

import java.util.List;

public record VoteBallotCreateRequest (
        List<Long> options
){}
