package com.mini.buting.global.security.config;

import com.mini.buting.global.property.WebUrlProperties;
import com.mini.buting.global.security.config.oauth2.OAuth2SelectAccountRequestResolver;
import com.mini.buting.global.security.filter.JwtAuthenticationFilter;
import com.mini.buting.global.security.filter.SecurityExceptionFilter;
import com.mini.buting.global.security.handler.CustomAccessDeniedHandler;
import com.mini.buting.global.security.handler.CustomAuthenticationEntryPoint;
import com.mini.buting.global.security.handler.oauth2.OAuth2FailureHandler;
import com.mini.buting.global.security.handler.oauth2.OAuth2SuccessHandler;
import com.mini.buting.global.security.property.CorsProperties;
import com.mini.buting.global.security.property.SecurityProperties;
import com.mini.buting.global.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Locale;

/**
 * <h2>Spring Security 설정</h2>
 * <p>
 * <hr/>
 * <h5>Filter Chain 우선순위</h5>
 * <ol>
 *     <li>{@link SecurityExceptionFilter}: Security Filter 단계 예외를 BaseResponse로 변환</li>
 *     <li>{@link JwtAuthenticationFilter}: Authorization 헤더 기반 인증 시도 및 SecurityContext 바인딩</li>
 *     <li>Spring Security 내부 인가 필터(AuthorizationFilter): 접근 허용/차단 결정</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({SecurityProperties.class, WebUrlProperties.class})
public class SecurityConfig {

    /* properties */
    private final SecurityProperties securityProperties;

    /* filters */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionFilter securityExceptionFilter;

    /* handlers */
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /* oauth2 */
    private final CustomOAuth2UserService oAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    /**
     * 애플리케이션 Security Filter Chain 구성
     *
     * <h5>구성 요소</h5>
     * <ul>
     *     <li>기본 보안: CORS 적용, CSRF/FormLogin/HttpBasic 비활성화, Stateless 세션 정책</li>
     *     <li>경로별 인가 규칙: whitelist 및 role 기반 접근 제어</li>
     *     <li>OAuth2 로그인: authorization endpoint 커스터마이징, userService, success/failure handle</li>
     *     <li>커스텀 필터: SecurityExceptionFilter -> JwtAuthenticationFilter 순으로 등록</li>
     *     <li>예외 처리: 401, 403 응답 표준화(BaseResponse)</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /* 기본 보안 설정 */
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* 경로별 인가 규칙 설정 */
                .authorizeHttpRequests(auth -> {
                    // 화이트리스트(permitAll) 설정
                    securityProperties.whitelist().values().forEach((methodStr, urls) -> {
                        if (urls == null || urls.isEmpty()) return;

                        String[] patterns = urls.stream()
                                .filter(StringUtils::hasText)
                                .map(String::trim)
                                .toArray(String[]::new);

                        if (patterns.length == 0) return;

                        auth.requestMatchers(HttpMethod.valueOf(methodStr.toUpperCase(Locale.ROOT)), patterns)
                                .permitAll();
                    });

                    // 권한별(hasRole) 설정
                    if (securityProperties.accessRules() != null) {
                        securityProperties.accessRules().forEach(rule -> {
                            String[] paths = rule.paths().stream()
                                    .filter(StringUtils::hasText)
                                    .map(String::trim)
                                    .toArray(String[]::new);

                            if (paths.length > 0) {
                                auth.requestMatchers(paths).hasRole(rule.role().toUpperCase(Locale.ROOT));
                            }
                        });
                    }

                    auth.anyRequest().authenticated();
                })

                /* OAuth2 설정 */
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(
                                new OAuth2SelectAccountRequestResolver(
                                        clientRegistrationRepository,
                                        securityProperties.oauth2().endpoint().authorization()
                                )
                        ))
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri(securityProperties.oauth2().endpoint().redirection() + "/*"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                /* Custom Filters */
                .addFilterBefore(securityExceptionFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, SecurityExceptionFilter.class)

                /* Exception Handler */
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint) // 인증 실패(401)
                        .accessDeniedHandler(accessDeniedHandler)           // 인가 실패(403)
                ).build();
    }

    /**
     * 정적 리소스에 대해 Spring Security Filter Chain을 적용하지 않음
     *
     * <p>{@code /css}, {@code /js}, {@code /images} 등 공통 정적 리소스 경로는 인증/인가와 무관하므로
     * Security Filter Chain에서 불필요한 오버헤드를 줄임</p>
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /**
     * CORS 정책 설정
     *
     * @implNote {@code allowCredentials=true}인 경우 {@code allowedOrigins}에 와일드카드(*) 사용 불가
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration conf = new CorsConfiguration();
        CorsProperties properties = securityProperties.cors();

        conf.setAllowedOrigins(properties.allowedOrigins());
        conf.setAllowedMethods(properties.allowedMethods());
        conf.setAllowedHeaders(properties.allowedHeaders());
        conf.setAllowCredentials(properties.allowCredentials());
        conf.setExposedHeaders(properties.exposedHeaders());
        conf.setMaxAge(properties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", conf);

        return source;
    }
}
