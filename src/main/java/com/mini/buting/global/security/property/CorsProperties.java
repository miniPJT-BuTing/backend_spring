package com.mini.buting.global.security.property;

import java.util.List;

/**
 * <h2>CORS 설정 정보</h2>
 *
 * @param allowedOrigins   자원 공유 허용할 출처(Origin) 리스트
 * @param allowedMethods   허용할 HTTP 메서드 목록
 * @param allowedHeaders   허용할 요청 헤더 리스트
 * @param allowCredentials 자격 증명 포함 허용 여부
 * @param exposedHeaders   브라우저가 접근할 수 있도록 노출할 응답 헤더 리스트
 * @param maxAge           Preflight 요청 결과의 캐싱 유효 기간
 */
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        List<String> exposedHeaders,
        Long maxAge
) {
}
