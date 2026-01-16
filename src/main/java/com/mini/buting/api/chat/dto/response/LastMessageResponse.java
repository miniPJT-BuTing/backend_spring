package com.mini.buting.api.chat.dto.response;

import java.time.LocalDateTime;

public record LastMessageResponse (
        String preview,
        LocalDateTime sentAt,
        Long seq
){}
