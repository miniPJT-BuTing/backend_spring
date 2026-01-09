package com.mini.buting.api.friend.dto.response;

import com.mini.buting.api.friend.domain.Friend;
import com.mini.buting.api.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 친구 목록 응답 DTO
 */
public record FriendListResponseDto(
        FriendInfo friendInfo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime becameFriendsAt
) {
    public record FriendInfo(
            Long memberId,
            String nickname,
            String universityName,
            String collegeName,
            Integer entryYear,
            Integer age,
            String gender,
            String mbti,
            String bio,
            String faceShapeName,
            List<String> personalityTypes
    ) {}

    public static FriendListResponseDto from(Friend friendship, Member currentMember) {
        Member friendMember = friendship.getOtherMember(currentMember);
        var university = friendMember.getUniversity();
        var college = friendMember.getCollege();
        var faceShape = friendMember.getFaceShape();

        return new FriendListResponseDto(
                new FriendInfo(
                        friendMember.getId(),
                        friendMember.getNickname(),
                        university != null ? university.getName() : null,
                        college != null ? college.getName() : null,
                        friendMember.getEntryYear(),
                        friendMember.getAge(),
                        friendMember.getGender().name(),
                        friendMember.getMbti().name(),
                        friendMember.getBio(),
                        faceShape != null ? faceShape.getName() : null,
                        friendMember.getPersonalityCodes()
                ),
                friendship.getCreatedAt()
        );
    }
}
