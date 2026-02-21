package com.mini.buting.global.security.handler.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;

/**
 * <h2>OAuth2 성공 처리 세부 전략 interface</h2>
 *
 * <p>특정 조건에 따른 개별 성공 처리 로직을 정의</p>
 */
public interface OAuth2SuccessHandlerStrategy {
    /**
     * 현재 인증 정보와 요청 상태가 해당 전략을 수행하기에 적합한지 판단
     *
     * @param authentication 인증 정보
     * @param request        HTTP 요청 객체
     * @return 처리 가능 여부
     */
    boolean supports(Authentication authentication, HttpServletRequest request);

    /**
     * 성공 후속 로직
     *
     * @param authentication 인증 정보
     * @param request        HTTP 요청 객체
     * @param response       HTTP 응답 객체
     * @throws IOException 입출력 예외 발생 시
     */
    void handle(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
