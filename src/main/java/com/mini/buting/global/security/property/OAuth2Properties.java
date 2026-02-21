package com.mini.buting.global.security.property;

/**
 * <h2>OAuth2 관련 커스텀 경로 설정 레코드</h2>
 *
 * @param endpoint 서버 내부 OAuth2 엔드포인트
 * @param client   인증 성공 후 리다이렉트될 클라이언트 경로
 */
public record OAuth2Properties(
        Endpoint endpoint,
        Client client
) {
    /**
     * 서버 내부 엔드포인트
     */
    public record Endpoint(
            String authorization,
            String redirection
    ) {
    }

    /**
     * 클라이언트 리다이렉트 엔드포인트
     */
    public record Client(String successUrl) {
    }
}
