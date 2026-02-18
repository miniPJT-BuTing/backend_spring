package com.mini.buting.api.auth.service;

import com.mini.buting.api.auth.dto.request.EmailCodeRequest;
import com.mini.buting.api.auth.dto.request.EmailVerifyRequest;
import com.mini.buting.api.auth.dto.response.EmailCodeResponse;
import com.mini.buting.api.auth.dto.response.EmailVerifyResponse;
import com.mini.buting.api.university.dto.ResolvedUniversity;
import com.mini.buting.api.university.service.UniversityDomainQueryService;
import com.mini.buting.global.mail.dto.MailContext;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.mail.dto.verification.VerificationCode;
import com.mini.buting.global.mail.service.MailSendService;
import com.mini.buting.global.mail.service.MailVerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <h2>인증 이메일 비즈니스 로직 서비스</h2>
 * <p>이메일 인증코드의 발급, 전송 및 검증 프로세스를 조율(오케스트레이션)하는 역할을 수행</p>
 * <p>대학 도메인 검증이 필요한 회원가입 절차와 일반적인 메일 인증 절차를 구분하여 처리하며,
 * {@link MailVerificationCodeService}와 {@link MailSendService}를 연동하여 인증울 관리</p>
 */
@Service
@RequiredArgsConstructor
public class AuthEmailVerificationService {

    private final UniversityDomainQueryService universityDomainQueryService;
    private final MailVerificationCodeService mailVerificationCodeService;
    private final MailSendService mailSendService;

    /**
     * <h3>인증코드 발급 및 메일 발송</h3>
     * <p>인증 타입을 확인하여 코드를 발급하고 해당 이메일로 인증 메일을 전송</p>
     * <p>회원가입({@link MailType#SIGN_UP}) 요청인 경우, 발급 전 해당 이메일 도메인이
     * 서비스에서 지원하는 대학 도메인인지 선제적으로 검증</p>
     */
    public EmailCodeResponse issueAndSendCode(EmailCodeRequest requestDto) {
        MailType mailType = MailType.from(requestDto.verificationType());

        // 회원가입 시 대학 도메인 가용 여부 사전 검증
        if (mailType == MailType.SIGN_UP) {
            universityDomainQueryService.resolveByEmail(requestDto.email());
        }

        // 인증코드 발급(Redis 저장)
        VerificationCode verificationCode = mailVerificationCodeService.issueVerificationCode(requestDto.email(), mailType);

        // 메일 발송
        MailContext context = new MailContext().withVerificationCode(verificationCode);
        mailSendService.sendMail(requestDto.email(), mailType, context);

        return EmailCodeResponse.of(mailType, verificationCode, LocalDateTime.now());
    }

    /**
     * <h3>인증코드 유효성 검증</h3>
     * <p>사용자가 입력한 코드를 검증하고, 성공 시 해당 인증 타입에 따른 부가 정보를 반환</p>
     * <p>회원가입({@link MailType#SIGN_UP}) 인증의 경우, 최종 가입 단계에서 필요한
     * 대학 도메인 식별 정보({@link ResolvedUniversity})를 함께 추출</p>
     */
    public EmailVerifyResponse verifyCode(EmailVerifyRequest requestDto) {
        MailType mailType = MailType.from(requestDto.verificationType());

        // 코드 일치 여부 및 시도 횟수 검증
        boolean verified = mailVerificationCodeService.verifyCode(
                requestDto.email(),
                mailType,
                requestDto.code()
        );

        // 회원가입일 경우 대학 정보와 함께 응답
        if (mailType == MailType.SIGN_UP) {
            ResolvedUniversity resolved = universityDomainQueryService.resolveByEmail(requestDto.email());
            return EmailVerifyResponse.of(mailType, verified, resolved);
        }

        return EmailVerifyResponse.of(mailType, verified);
    }
}
