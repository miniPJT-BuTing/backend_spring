package com.mini.buting.global.util;

import com.mini.buting.global.property.CookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * 쿠키 생성 및 관리를 위한 유틸리티 클래스
 * - {@link CookieProperties}에 정의된 전역 정책을 따름
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({CookieProperties.class})
public class CookieUtils {
    private final CookieProperties cookieProperties;

    /**
     * 기본 전역 정책을 따르는 쿠키 생성
     */
    public void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        setCookie(response, name, value, maxAge, cookieProperties.sameSite());
    }

    /**
     * 특정 SameSite 정책이 필요한 경우 직접 호출할 것
     */
    public void setCookie(HttpServletResponse response, String name, String value, int maxAge, String sameSite) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .secure(cookieProperties.secure())
                .sameSite(sameSite)                 // 상황에 따라 명시적 값 적용
                .domain(cookieProperties.domain())
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
