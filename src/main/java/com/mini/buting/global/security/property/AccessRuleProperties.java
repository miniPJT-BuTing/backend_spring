package com.mini.buting.global.security.property;

import java.util.List;

/**
 * 권한 별 인가 경로 설정
 */
public record AccessRuleProperties() {
    public record AccessRule(
            String role,
            List<String> paths
    ) {
    }
}
