package com.mini.buting.global.util;

import com.mini.buting.global.property.CookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * <h2>쿠키 생성 및 관리 유틸리티</h2>
 *
 * <p>{@link CookieProperties}에 정의된 전역 보안 정책(SameSite, Secure, HttpOnly 등)을 준수</p>
 * <p>
 * <hr/>
 * <h5>핵심 보안 정책</h5>
 * <ul>
 * <li><b>HttpOnly: </b>JavaScript를 통한 쿠키 접근을 차단하여 XSS 공격 방어</li>
 * <li><b>Secure: b>HTTPS 연결에서만 쿠키가 전송되도록 보장</li>
 * <li><b>SameSite: </b>CSRF 공격 방지를 위해 설정된 SameSite 정책을 적용</li>
 * </ul>
 *
 * @see CookieProperties
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({CookieProperties.class})
public class CookieUtils {
    private final CookieProperties cookieProperties;

    /**
     * 기본 전역 정책을 따르는 쿠키를 생성하여 응답 헤더에 추가
     *
     * @param response HTTP 응답 객체
     * @param name     쿠키명
     * @param value    쿠키에 담을 값
     * @param maxAge   쿠키 유효 기간(초 단위)
     */
    public void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        setCookie(response, name, value, maxAge, cookieProperties.sameSite());
    }

    /**
     * 상황에 따라 명시적인 SameSite 정책을 사용하여 보안 쿠키를 생성
     *
     * @param response HTTP 응답 객체
     * @param name     쿠키명
     * @param value    쿠키에 담을 값
     * @param maxAge   쿠키 유효 기간(초 단위)
     * @param sameSite 적용할 SameSite 정책 (None, Lax, Strict)
     */
    public void setCookie(HttpServletResponse response, String name, String value, int maxAge, String sameSite) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .secure(cookieProperties.secure())
                .sameSite(sameSite)
                .domain(cookieProperties.domain())
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
