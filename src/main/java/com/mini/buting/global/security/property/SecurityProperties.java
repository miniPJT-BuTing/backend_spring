package com.mini.buting.global.security.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        JwtProperties jwt,
        WhitelistProperties whitelist,
        CorsProperties cors,
        List<AccessRuleProperties.AccessRule> accessRules
) {
}
