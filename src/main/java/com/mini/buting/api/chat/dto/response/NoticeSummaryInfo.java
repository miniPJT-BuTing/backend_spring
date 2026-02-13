package com.mini.buting.api.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "공지 요약 정보")
public record NoticeSummaryInfo(
        @Schema(description = "장소", example = "서면역 2번 출구")
        String place,

        @Schema(description = "만나는 시간", example = "2026-02-10T19:30:00")
        LocalDateTime meetAt,

        @Schema(description = "공지 설명", example = "늦으면 벌금 5천원")
        String description,

        @Schema(description = "공지 수정 시각", example = "2026-01-21T16:00:00")
        LocalDateTime updatedAt
) {}
