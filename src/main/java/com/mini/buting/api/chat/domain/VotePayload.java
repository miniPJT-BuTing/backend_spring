package com.mini.buting.api.chat.domain;

public record VotePayload(Long voteId, String title) implements Payload {
}
