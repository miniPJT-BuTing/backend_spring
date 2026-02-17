package com.mini.buting.api.auth.dto.request;

import com.mini.buting.global.constant.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * <h2>이메일 코드 생성/전송 요청 DTO</h2>
 *
 * @param email            이메일 코드를 요청하는 이메일
 * @param verificationType 이메일 인증 타입
 */
public record EmailCodeRequest(
        @NotBlank(message = ErrorMessages.EMAIL_NOT_FOUND)
        @Email(message = ErrorMessages.INVALID_EMAIL)
        String email,

        @NotBlank(message = ErrorMessages.VERIFICATION_TYPE_NOT_FOUND)
        String verificationType
) {
}
