package com.mini.buting.global.mail.provider.verification;

import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.property.WebUrlProperties;
import org.springframework.stereotype.Component;

/**
 * <h2>회원가입 인증 메일 변수 제공 Provider</h2>
 * <p>회원가입 인증 메일 타입과 인증메일 공통 변수 조합 로직을 연결</p>
 *
 * @see MailType#SIGN_UP
 * @see AbstractVerificationVariablesProvider
 */
@Component
public class SignUpVariablesProvider extends AbstractVerificationVariablesProvider {
    /**
     * 회원가입 인증 메일 Provider 생성
     *
     * @param webUrlProperties 서비스 웹 URL 설정
     */
    public SignUpVariablesProvider(WebUrlProperties webUrlProperties) {
        super(webUrlProperties);
    }

    /**
     * 현재 Provider가 담당하는 메일 타입을 반환
     *
     * @return {@link MailType#SIGN_UP}
     */
    @Override
    public MailType getMailType() {
        return MailType.SIGN_UP;
    }
}
