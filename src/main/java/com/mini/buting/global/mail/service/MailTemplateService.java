package com.mini.buting.global.mail.service;

import com.mini.buting.global.constant.Patterns;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.response.BaseResponseStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * <h2>메일 템플릿 렌더링 서비스</h2>
 */
@Service
public class MailTemplateService {

    public String render(MailType mailType, Map<String, Object> variables) {
        String template = loadTemplate(mailType.getTemplatePath());
        return replaceVariables(template, variables);
    }

    // --- Helper Methods ---
    private Resource loadTemplateResource(String templatePath) {
        return new ClassPathResource(templatePath);
    }

    /**
     * 클래스패스 템플릿 파일을 {@code UTF-8}로 읽음
     *
     * @param templatePath 템플릿 경로
     * @return 템플릿 문자열
     */
    private String loadTemplate(String templatePath) {
        Resource resource = loadTemplateResource(templatePath);

        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BaseException(BaseResponseStatus.MAIL_SEND_FAIL);
        }
    }

    /**
     * 템플릿의 {{key}} place holder을 변수값으로 치환
     *
     * @param template  원본 템플릿
     * @param variables 변수 맵
     * @return 치환된 템플릿
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        Matcher matcher = Patterns.MAIL_TEMPLATE_PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables.get(key);
            if (value == null) {
                throw new BaseException(BaseResponseStatus.MAIL_SEND_FAIL);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
