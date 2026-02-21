package com.mini.buting.global.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.web-url")
public record WebUrlProperties(
        String main,
        String logo
) {
}
