package com.mini.buting.api.chat.domain.payload;

import java.util.List;

public record VotePayload(
        VoteAction action,
        String voteId,
        String title,
        List<String> options,
        List<String> result
) implements Payload {
}
