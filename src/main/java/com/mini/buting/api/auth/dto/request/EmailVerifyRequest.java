package com.mini.buting.api.auth.dto.request;

import com.mini.buting.global.constant.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * <h2>이메일 인증코드 검증 요청 DTO</h2>
 *
 * @param email            인증 대상 이메일
 * @param verificationType 인증 타입
 * @param code             인증코드
 */
public record EmailVerifyRequest(
        @NotBlank(message = ErrorMessages.EMAIL_NOT_FOUND)
        @Email(message = ErrorMessages.INVALID_EMAIL)
        String email,

        @NotBlank(message = ErrorMessages.VERIFICATION_TYPE_NOT_FOUND)
        String verificationType,

        @NotBlank(message = ErrorMessages.VERIFICATION_CODE_NOT_FOUND)
        String code
) {
}
