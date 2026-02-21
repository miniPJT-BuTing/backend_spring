package com.mini.buting.global.mail.provider;

import com.mini.buting.global.mail.dto.MailContext;
import com.mini.buting.global.mail.dto.MailType;

import java.util.Map;

/**
 * <h2>메일 템플릿 변수 제공 전략 인터페이스</h2>
 */
public interface MailValueProvider {
    public MailType getMailType();

    public Map<String, Object> resolveTemplateVariables(MailContext context);
}
