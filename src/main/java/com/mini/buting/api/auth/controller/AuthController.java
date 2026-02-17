package com.mini.buting.api.auth.controller;

import com.mini.buting.api.auth.dto.request.EmailCodeRequest;
import com.mini.buting.api.auth.dto.request.EmailVerifyRequest;
import com.mini.buting.api.auth.dto.response.EmailCodeResponse;
import com.mini.buting.api.auth.dto.response.EmailVerifyResponse;
import com.mini.buting.api.auth.service.AuthTokenService;
import com.mini.buting.global.mail.dto.MailContext;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.mail.dto.verification.VerificationCode;
import com.mini.buting.global.mail.service.MailSendService;
import com.mini.buting.global.mail.service.MailVerificationCodeService;
import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.security.constant.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "사용자 인증/인가 관련 API")
public class AuthController {
    private final AuthTokenService authTokenService;
    private final MailVerificationCodeService mailVerificationCodeService;
    private final MailSendService mailSendService;

    @Operation(summary = "JWT 재발급 API", description = "RT로 만료된 AT를 새로 발급")
    @PostMapping("/reissue")
    public BaseResponse<Void> refresh(
            @CookieValue(value = SecurityConstants.Token.REFRESH_COOKIE_NAME) String refreshToken,
            HttpServletResponse response) {
        authTokenService.refreshToken(response, refreshToken);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "로그아웃 API", description = "기존에 사용하던 AT/RT를 무효화")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authTokenService.logout(request, response);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "이메일 인증코드 발급/전송 API", description = "회원가입, 분실 비밀번호 재설정 등 모든 이메일 인증 요청에 공통으로 사용됨.")
    @PostMapping("/email-verifications")
    public BaseResponse<EmailCodeResponse> sendCodeToEmail(@Valid @RequestBody EmailCodeRequest requestDto) {
        MailType mailType = MailType.from(requestDto.verificationType());
        VerificationCode verificationCode = mailVerificationCodeService.issueVerificationCode(requestDto.email(), mailType);

        MailContext context = new MailContext().withVerificationCode(verificationCode);
        mailSendService.sendMail(requestDto.email(), mailType, context);

        return BaseResponse.onSuccess(EmailCodeResponse.of(mailType, verificationCode, LocalDateTime.now()));
    }

    @Operation(summary = "이메일 인증코드 검증 API", description = """
            발급된 이메일 인증코드를 검증(모든 이메일 코드 검증 요청에 공통으로 사용).
            
            본 API는 시도 횟수를 차감하므로 멱등하지 않음을 주의(동일 요청을 반복 호출해도 같은 결과가 보장되지 않음).
            
            인증 성공 시 코드는 즉시 만료(1회성) 처리됨.
            """)
    @PutMapping("/email-verifications")
    public BaseResponse<EmailVerifyResponse> verifyEmailCode(@Valid @RequestBody EmailVerifyRequest requestDto) {
        MailType mailType = MailType.from(requestDto.verificationType());
        boolean verified = mailVerificationCodeService.verifyCode(requestDto.email(), mailType, requestDto.code());
        return BaseResponse.onSuccess(EmailVerifyResponse.of(mailType, verified));
    }

}
