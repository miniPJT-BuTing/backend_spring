package com.mini.buting.api.chat.domain.payload;

public record VotePayload(Long voteId, String title) implements Payload {
}
