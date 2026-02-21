package com.mini.buting.api.auth.controller;

import com.mini.buting.api.auth.dto.request.EmailCodeRequest;
import com.mini.buting.api.auth.dto.request.EmailVerifyRequest;
import com.mini.buting.api.auth.dto.response.EmailCodeResponse;
import com.mini.buting.api.auth.dto.response.EmailVerifyResponse;
import com.mini.buting.api.auth.service.AuthEmailVerificationService;
import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth/email-verifications")
@Tag(name = "Auth - Email", description = "이메일 기반 본인 인증 API")
public class EmailVerificationController {
    private final AuthEmailVerificationService authEmailVerificationService;

    @Operation(summary = "이메일 인증코드 발급/전송 API", description = "회원가입, 분실 비밀번호 재설정 등 모든 이메일 인증 요청에 공통으로 사용됨.")
    @PostMapping()
    public BaseResponse<EmailCodeResponse> sendCodeToEmail(@Valid @RequestBody EmailCodeRequest requestDto) {
        return BaseResponse.onSuccess(authEmailVerificationService.issueAndSendCode(requestDto));
    }

    @Operation(summary = "이메일 인증코드 검증 API", description = """
            발급된 이메일 인증코드를 검증(모든 이메일 코드 검증 요청에 공통으로 사용).
            
            본 API는 시도 횟수를 차감하므로 멱등하지 않음을 주의(동일 요청을 반복 호출해도 같은 결과가 보장되지 않음).
            
            인증 성공 시 코드는 즉시 만료(1회성) 처리됨.
            """)
    @PutMapping()
    public BaseResponse<EmailVerifyResponse> verifyEmailCode(@Valid @RequestBody EmailVerifyRequest requestDto) {
        return BaseResponse.onSuccess(authEmailVerificationService.verifyCode(requestDto));
    }
}
