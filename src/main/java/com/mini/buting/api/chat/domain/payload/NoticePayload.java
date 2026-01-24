package com.mini.buting.api.chat.domain.payload;

import com.mini.buting.api.chat.dto.response.NoticeAction;

import java.time.LocalDateTime;

public record NoticePayload(String place, LocalDateTime meetAt, String text, NoticeAction action) implements Payload {
}
