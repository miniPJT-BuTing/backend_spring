package com.mini.buting.api.chat.dto.request;

import java.time.LocalDateTime;

public record NoticeUpsertRequest(
        String place,
        LocalDateTime meetAt,
        String description
){
}
