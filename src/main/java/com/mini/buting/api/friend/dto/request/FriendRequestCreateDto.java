package com.mini.buting.api.friend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 친구 요청 생성 DTO
 */
public record FriendRequestCreateDto(
        @NotBlank(message = "친구 요청을 받을 사용자의 닉네임은 필수입니다.")
//        @Size(max = 10, message = "닉네임은 10자를 초과할 수 없습니다.")
        String targetNickname
) {
}
