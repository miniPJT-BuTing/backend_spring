package com.mini.buting.api.chat.dto.response;

import java.time.LocalDateTime;

public record NoticeViewResponse(
        String roomId,
        String place,
        LocalDateTime meetAt,
        String description,
        LocalDateTime updatedAt,
        String updatedBy,
        Long updatedById
) {}
