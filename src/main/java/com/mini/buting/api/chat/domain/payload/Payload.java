package com.mini.buting.api.chat.domain.payload;

public sealed interface Payload permits TextPayload, ImagePayload, NoticePayload, VotePayload {}
