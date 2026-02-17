package com.mini.buting.global.mail.service;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.mail.constants.MailConstants;
import com.mini.buting.global.mail.dto.MailContext;
import com.mini.buting.global.mail.dto.MailType;
import com.mini.buting.global.mail.provider.MailValueProvider;
import com.mini.buting.global.response.BaseResponseStatus;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <h2>메일 발송 서비스</h2>
 */
@Slf4j
@Service
public class MailSendService {
    private final JavaMailSender javaMailSender;
    private final Map<MailType, MailValueProvider> providerMap;
    private final MailTemplateService mailTemplateService;

    public MailSendService(
            JavaMailSender javaMailSender,
            MailTemplateService mailTemplateService,
            List<MailValueProvider> providers
    ) {
        this.javaMailSender = javaMailSender;
        this.mailTemplateService = mailTemplateService;
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(MailValueProvider::getMailType, Function.identity()));
    }

    /**
     * 일반 텍스트 메일 발송
     *
     * @param to      수신자
     * @param subject 제목
     * @param content 본문
     */
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();

        try {
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("{} sendSimpleMail failed. to={}, subject={}",
                    MailConstants.Log.LOG_PREFIX, to, subject, e);
            throw new BaseException(BaseResponseStatus.MAIL_SEND_FAIL);
        }
    }

    public void sendMail(String to, MailType mailType, MailContext context) {
        MailValueProvider provider = findProviderOrThrow(mailType);
        Map<String, Object> values = provider.resolveTemplateVariables(context);
        sendTemplatedHtml(to, mailType, values);
    }

    public void sendTemplatedHtml(String to, MailType mailType, Map<String, Object> values) {
        String subject = mailType.getSubject();
        String content = mailTemplateService.render(mailType, values);
        sendHtmlMailInternal(to, subject, content, mailType);
    }

    // --- Helper Methods ---
    private MailValueProvider findProviderOrThrow(MailType mailType) {
        MailValueProvider provider = providerMap.get(mailType);
        if (provider == null) {
            throw new BaseException(BaseResponseStatus.UNSUPPORTED_MAIL_TYPE);
        }
        return provider;
    }

    private void sendHtmlMailInternal(String to, String subject, String content, MailType mailType) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("{} sendHtmlMail failed. to={}, subject={}, mailType={}",
                    MailConstants.Log.LOG_PREFIX, to, subject, mailType, e);
            throw new BaseException(BaseResponseStatus.MAIL_SEND_FAIL);
        }
    }
}
