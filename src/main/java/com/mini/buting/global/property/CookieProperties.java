package com.mini.buting.global.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import com.mini.buting.global.util.CookieUtils;

/**
 * 서버 공용 cookie 설정
 * - 쿠키 생성은 {@link CookieUtils}에서 진행됨
 */
@ConfigurationProperties(prefix = "server.cookie")
public record CookieProperties(
        boolean secure,
        String sameSite,
        String domain
) {
}
