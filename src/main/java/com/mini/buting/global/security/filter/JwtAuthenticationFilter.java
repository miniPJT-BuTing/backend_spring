package com.mini.buting.global.security.filter;

import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.provider.JwtTokenProvider;
import com.mini.buting.global.security.service.JwtAuthenticationService;
import com.mini.buting.global.security.service.TokenBlacklistService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <h2>JWT 인증 필터</h2>
 * <p>모든 요청에 대해 실행되며, HTTP 헤더의 AccessToken을 확인하고,
 * 토큰이 존재하는 경우에만 인증을 수행하여 {@link org.springframework.security.core.context.SecurityContext}에
 * {@link Authentication}을 저장함.</p>
 * <hr/>
 * <h5>역할</h5>
 * <ol>
 *     <li>AccessToken 추출</li>
 *     <li>블랙리스트 여부 확인</li>
 *     <li>토큰 파싱/클레임 검증 및 Authentication 생성</li>
 *     <li>성공 시 {@link org.springframework.security.core.context.SecurityContext}에 인증 객체 저장</li>
 * </ol>
 * <hr/>
 * <h5>[참고] 예외 처리 정책</h5>
 * <ul>
 *     <li><strong>permitAll 경로: </strong>토큰이 없거나 토큰 오류가 발생해도 인증 없이 요청을 통과시킴</li>
 *     <li><strong>보호 경로: </strong>토큰 오류 발생 시 예외를 전파하며, {@link SecurityExceptionFilter}가 표준화된 BaseResponse 형태로 응답을 작성함.</li>
 * </ul>
 * <hr/>
 * <h5>[참고] 인가(Authorization) 처리 흐름</h5>
 * <ul>
 *     <li>경로별 접근 허용/차단(permitAll/authenticated/hasRole)은 {@code SecurityConfig.authorizeHttpRequests(...)}에서 정의되며,</li>
 *     <li>실제 집행은 Spring Security 내부 {@code AuthorizationFilter}가 담당</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        boolean permitAll = isPermitAll(request);
        String token = tokenProvider.getTokenFromRequest(request);

        // 토큰이 없으면 인증 객체를 만들지 않고 진행(인가 판단은 AuthorizationFilter가 수행)
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
            }

            Authentication auth = jwtAuthenticationService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            if (permitAll) {
                filterChain.doFilter(request, response);
                return;
            }
            throw e;
        }
    }

    /**
     * WhitelistProperties 기반 AntPath 맵핑 검사
     */
    private boolean isPermitAll(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        return securityProperties.whitelist().values().entrySet().stream()
                .anyMatch(entry ->
                        entry.getKey().equalsIgnoreCase(method) &&
                                entry.getValue().stream().anyMatch(pattern -> antPathMatcher.match(pattern, uri))
                );
    }
}
