package com.mini.buting.api.chat.dto.response;

import java.time.LocalDateTime;

public record NoticeViewResponse(
        String place,
        LocalDateTime meetAt,
        String description,
        LocalDateTime updatedAt,
        String updatedBy,
        Long updatedById
) {}
