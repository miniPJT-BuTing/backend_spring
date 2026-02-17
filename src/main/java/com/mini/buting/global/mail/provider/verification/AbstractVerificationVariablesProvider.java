package com.mini.buting.global.mail.provider.verification;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.mail.constants.MailConstants;
import com.mini.buting.global.mail.dto.MailContext;
import com.mini.buting.global.mail.dto.VerificationCode;
import com.mini.buting.global.mail.provider.MailValueProvider;
import com.mini.buting.global.property.WebUrlProperties;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * <h2>인증 메일 변수 조합 공통 추상 클래스</h2>
 */
@RequiredArgsConstructor
public abstract class AbstractVerificationVariablesProvider implements MailValueProvider {

    private final WebUrlProperties webUrlProperties;

    /**
     * 인증 메일 템플릿 공통 변수 맵을 생성
     *
     * @param context 메일 내용
     * @return 템플릿 변수 맵
     * @throws BaseException 인증코드가 컨텍스트에 없는 경우
     */
    @Override
    public Map<String, Object> resolveTemplateVariables(MailContext context) {
        VerificationCode code = context.getVerificationCode()
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MAIL_SEND_FAIL));

        return Map.of(
                MailConstants.Key.MAIN_URL, webUrlProperties.main(),
                MailConstants.Key.LOGO_IMAGE_URL, webUrlProperties.logo(),
                MailConstants.Key.CODE, code.code()
        );
    }
}
