package com.mini.buting.global.security.property;

import java.util.List;

/**
 * <h2>권한 기반 인가 규칙 설정 레코드</h2>
 *
 * <p>애플리케이션 전역에서 사용되는 권한별 접근 제어 목록 정의
 * 설정 파일{@code yaml}, {@code env}을 통해 각 역할(Role)이 접근 가능한 경로들을 구조적으로 관리하며,
 * 보안 설정 클래스에서 인가 정책을 동적으로 구성할 때 사용
 * </p>
 */
public record AccessRuleProperties() {
    /**
     * 특정 역할에 할당된 인가 경로 정보를 담는 내부 레코드
     *
     * @param role  사용자 권한 명칭("USER', "ADMIN" 등...)
     * @param paths 해당 권한이 접근 가능한 자원 경로
     */
    public record AccessRule(
            String role,
            List<String> paths
    ) {
    }
}
