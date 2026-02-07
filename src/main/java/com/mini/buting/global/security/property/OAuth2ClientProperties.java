package com.mini.buting.global.security.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * * <h2>OAuth2 클라이언트 설정 정보 레코드</h2>
 * * <p>외부 소셜 로그인 제공자의 등록 정보(Registration)와 서비스 제공자(Provider) 정보 관리</p>
 *
 * @param registration 서비스 제공자별 클라이언트 등록 정보
 * @param provider     커스텀 서비스 제공자의 엔드포인트 정보 (Kakao 등 필수로 선언해야 하는 경우 사용)
 */
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public record OAuth2ClientProperties(
        Map<String, Registration> registration,
        Map<String, Provider> provider
) {
    /**
     * 각 서비스 제공자별 클라이언트 등록 정보
     * <p>특정 OAuth2 제공자에 대한 애플리케이션의 인증 식별 정보를 바인딩</p>
     *
     * @param clientId                   OAuth2 서버로부터 발급받은 클라이언트 ID
     * @param clientSecret               OAuth2 서버로부터 발급받은 클라이언트 보안 비밀번호
     * @param redirectUri                인증 성공 후, 권한 코드를 전달받을 애플리케이션의 URI 패턴
     * @param authorizationGrantType     권한 부여 방식
     * @param clientAuthenticationMethod 클라이언트 인증 방법
     * @param clientName                 인증 화면 등에 표시될 서비스 이름
     * @param scope                      리소스 서버로부터 요청할 사용자 권한 범위
     */
    public record Registration(
            String clientId,
            String clientSecret,
            String redirectUri,
            String authorizationGrantType,
            String clientAuthenticationMethod,
            String clientName,
            List<String> scope
    ) {
    }

    /**
     * 외부 인증 서버(OAuth2 Provider)의 엔드포인트 정보
     * <p>Spring Security가 기본 설정을 제공하지 않는 커스텀 제공자의 API 엔드포인트 정의</p>
     *
     * @param authorizationUri  인가 코드 발급을 위한 인증 서버 URI
     * @param tokenUri          AccessToken 발급을 위한 토큰 서버 API
     * @param userInfoUri       사용자 프로필 정보를 조회하는 API URI
     * @param userNameAttribute OAUth2 응답에서 사용자의 고유 식별자로 사용될 키 값
     */
    public record Provider(
            String authorizationUri,
            String tokenUri,
            String userInfoUri,
            String userNameAttribute
    ) {
    }
}
