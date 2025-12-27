package com.mini.buting.api.chat.domain;

import java.time.LocalDateTime;

public record NoticePayload(String place, LocalDateTime meetAt, String text) implements Payload {
}
