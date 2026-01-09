package com.mini.buting.api.friend.dto.response;

import com.mini.buting.api.friend.domain.FriendRequest;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 친구 요청 응답 DTO
 */
public record FriendRequestResponseDto(
        Long requestId,
        RequesterInfo requester,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime requestedAt,
        String status
) {
    public record RequesterInfo(
            Long memberId,
            String nickname,
            String universityName,
            String collegeName,
            Integer entryYear,
            String faceShapeName
    ) {}

    public static FriendRequestResponseDto from(FriendRequest friendRequest) {
        var requester = friendRequest.getRequester();
        var university = requester.getUniversity();
        var college = requester.getCollege();
        var faceShape = requester.getFaceShape();

        return new FriendRequestResponseDto(
                friendRequest.getId(),
                new RequesterInfo(
                        requester.getId(),
                        requester.getNickname(),
                        university != null ? university.getName() : null,
                        college != null ? college.getName() : null,
                        requester.getEntryYear(),
                        faceShape != null ? faceShape.getName() : null
                ),
                friendRequest.getCreatedAt(),
                friendRequest.getStatus().name()
        );
    }
}
