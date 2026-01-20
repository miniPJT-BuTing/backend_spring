package com.mini.buting.api.chat.dto.response;

import com.mini.buting.api.chat.domain.chatroom.Notice;

import java.time.LocalDateTime;

public record NoticeResponse(
        NoticeAction action,
        String roomId,
        String place,
        LocalDateTime meetAt,
        String description,
        LocalDateTime updatedAt,
        String updatedBy,
        Long updatedById
) {

    public static NoticeResponse created(Notice notice){
        return new NoticeResponse(
                NoticeAction.CREATED,
                notice.getId().toString(),
                notice.getPlace(),
                notice.getMeetAt(),
                notice.getDescription(),
                notice.getUpdatedAt(),
                notice.getUpdatedBy().getNickname(),
                notice.getUpdatedBy().getId()
        );
    }

    public static NoticeResponse updated(Notice notice){
        return new NoticeResponse(
                NoticeAction.UPDATED,
                notice.getId().toString(),
                notice.getPlace(),
                notice.getMeetAt(),
                notice.getDescription(),
                notice.getUpdatedAt(),
                notice.getUpdatedBy().getNickname(),
                notice.getUpdatedBy().getId()
        );
    }

    public static NoticeResponse viewed(Notice notice){
        return new NoticeResponse(
                NoticeAction.VIEWED,
                notice.getId().toString(),
                notice.getPlace(),
                notice.getMeetAt(),
                notice.getDescription(),
                notice.getUpdatedAt(),
                notice.getUpdatedBy().getNickname(),
                notice.getUpdatedBy().getId()
        );
    }
}
