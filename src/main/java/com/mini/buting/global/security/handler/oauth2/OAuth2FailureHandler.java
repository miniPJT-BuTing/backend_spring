package com.mini.buting.global.security.handler.oauth2;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.constant.OAuth2Constants;
import com.mini.buting.global.security.constant.SecurityConstants;
import com.mini.buting.global.security.property.SecurityProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * <h2>OAuth2 인증 실패 핸들러</h2>
 *
 * <p>소셜 로그인 과정에서 예외가 발생했을 때 호출되며,
 * PWA 클라이언트(모바일 웹)가 인지할 수 있도록 에러 정보를 쿼리 파라미터에 실어 리다이렉트함.</p>
 * <p>
 * <hr/>
 * <h5>주요 역할</h5>
 * <ul>
 * <li>발생한 예외로부터 시스템 내부 에러 코드 추출</li>
 * <li>Security 설정에 정의된 클라이언트 소셜 인증 결과 랜딩 페이지로 리다이렉트 경로 설정</li>
 * <li>인가 실패 상태({@code isSuccess=false})와 에러 코드({@code code}) 전달</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    private final SecurityProperties securityProperties;

    /**
     * OAuth2 인증 실패 시 호출돼 클라이언트로의 리다이렉트를 제어
     * <p>인증 과정에서 발생한 예외를 분석하여 에러 코드를 추출하고,
     * 환경 파일에 정의된 {@code success-url}을 기반으로 실패 정보가 담긴 쿼리 파라미터를 생성하여 전달</p>
     *
     * @param request   HTTP 요청 객체
     * @param response  HTTP 응답 객체 (리다이렉트 수행용)
     * @param exception 인증 과정 중 발생한 예외
     * @throws IOException      입출력 관련 예외 발생 시
     * @throws ServletException 서블릿 관련 예외 발생 시
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.warn("{} OAuth2 failure response already committed. method={}, uri={}",
                    SecurityConstants.Log.LOG_PREFIX, request.getMethod(), request.getRequestURI());
            return;
        }

        int errorCode = getErrorCode(exception);

        String redirectUri = securityProperties.oauth2().client().successUrl();
        String targetUri = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam(OAuth2Constants.Parameter.IS_SUCCESS, false)
                .queryParam(OAuth2Constants.Parameter.CODE, errorCode)
                .build().toUriString();

        response.sendRedirect(targetUri);
    }

    /**
     * 발생한 예외의 타입을 분석하여 적절한 응답 코드를 반환
     * <p><strong>예외 판별 우선순위:</strong>
     * <ol>
     * <li>{@link BaseException}: 의도적으로 발생시킨 비즈니스 예외</li>
     * <li>{@link InternalAuthenticationServiceException}: DB 연동 오류 등 서버 내부 시스템 장애({@code 500})</li>
     * <li>기타 {@link AuthenticationException}: 인증 실패({@code 401})</li>
     * </ol>
     * </p>
     *
     * @param exception 인증 과정 중 발생한 예외
     * @return {@link BaseResponseStatus}에 정의된 에러 코드({@code int})
     */
    private int getErrorCode(AuthenticationException exception) {
        BaseException be = findBaseException(exception);
        if (be != null) {
            return be.getStatus().getCode();
        }

        if (exception instanceof InternalAuthenticationServiceException) {
            return BaseResponseStatus.INTERNAL_SERVER_ERROR.getCode();
        }

        return BaseResponseStatus.AUTHENTICATION_FAILED.getCode();
    }

    private BaseException findBaseException(Throwable t) {
        for (Throwable cur = t; cur != null; cur = cur.getCause()) {
            if (cur instanceof BaseException be) {
                return be;
            }
        }
        return null;
    }
}
