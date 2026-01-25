package com.mini.buting.global.security.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * <h2>보안 설정 정보의 루트 레코드</h2>
 *
 * <p>설정 파일들의 {@code security} prefix를 기반으로 보안 관련 모든 세부 설정을 집약함
 * JWT, Whitelist, CORS 및 인가 규칙을 계층적으로 관리하여 코드 가독성과 타입 안정성을 높였음</p>
 * <p>
 * <hr/>
 * <h5>설계 의도</h5>
 * <ul>
 * <li>각각의 도메인별 설정의 하나의 루트 객체로 조립하여 관리의 편의성을 높이기 위해</li>
 * <li>런타임 중 설정값 오염 방지를 위해 Java Record 타입을 도입함</li>
 * </ul>
 *
 * @param jwt         JWT 발급 및 검증 관련 세부 설정
 * @param whitelist   인증이 불필요한 공용 API 경로 목록
 * @param cors        CORS 상세 정책
 * @param accessRules 권한({@code role})별 접근 제어 규칙 목록
 */
@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        JwtProperties jwt,
        WhitelistProperties whitelist,
        CorsProperties cors,
        List<AccessRuleProperties.AccessRule> accessRules
) {
}
