package com.mini.buting.api.chat.dto.response;

public record ChatReadEvent(
        String roomId,
        Long readerId,
        Long lastReadSeq
) {}
