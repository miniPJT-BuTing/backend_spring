package com.mini.buting.api.chat.domain;

public sealed interface Payload permits TextPayload, ImagePayload, NoticePayload, VotePayload {}
