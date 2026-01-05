package com.mini.buting.api.chat.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Embeddable
public class LastMessage {

    @Enumerated(EnumType.STRING)
    private MessageType type;
    private String preview;
    private LocalDateTime sentAt;
    private Long seq;
}
