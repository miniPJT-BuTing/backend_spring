package com.mini.buting.global.security.config.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * <h2>OAuth2 인증 요청 파라미터 커스텀 리졸버</h2>
 *
 * <p>소셜 로그인 인증 요청 시, Provider에게 전달할 추가 파라미터를 정의함.
 * 기본적으로 {@code prompt=select_account}를 추가하여, 브라우저에 세션이 있더라도 항상 계정 선택창이 나타나도록 함</p>
 * <p>
 * <hr/>
 * <h5>설계 이유</h5>
 * <ul>
 * <li>여러 소셜 계정을 사용하는 사용자가 의도치 않은 계정으로 자동 로그인되는 것을 방지하기 위해</li>
 * </ul>
 *
 * @see org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
 */
public class OAuth2SelectAccountRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final OAuth2AuthorizationRequestResolver defaultResolver;

    /**
     * @param repository                  클라이언트 등록 정보 저장소
     * @param authorizationRequestBaseUri OAuth2 로그인을 시작하는 Base URI
     */
    public OAuth2SelectAccountRequestResolver(ClientRegistrationRepository repository, String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return (req != null) ? customAuthorizationRequest(req) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return (req != null) ? customAuthorizationRequest(req) : null;
    }

    /**
     * 기존 인증 요청 객체에 추가 파라미터를 주입
     */
    private OAuth2AuthorizationRequest customAuthorizationRequest(OAuth2AuthorizationRequest oldReq) {
        Map<String, Object> additionalParams = new HashMap<>(oldReq.getAdditionalParameters());
        additionalParams.put("prompt", "select_account"); // 계정 선택 화면 강제

        return OAuth2AuthorizationRequest.from(oldReq)
                .additionalParameters(additionalParams)
                .build();
    }
}
