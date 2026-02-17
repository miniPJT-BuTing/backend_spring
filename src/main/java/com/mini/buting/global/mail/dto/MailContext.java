package com.mini.buting.global.mail.dto;

import com.mini.buting.global.mail.constants.MailConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MailContext {
    private final Map<String, Object> context = new HashMap<>();

    public MailContext withVerificationCode(VerificationCode code) {
        context.put(MailConstants.Key.CODE, code);
        return this;
    }

    public Optional<VerificationCode> getVerificationCode() {
        return Optional.ofNullable((VerificationCode) context.get(MailConstants.Key.CODE));
    }
}
