package com.mini.buting.global.security.property;

import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * permit-all 의 whitelist 목록
 * - 어떤 HttpMethod가 yaml에 설정되어 있던지, 범용적으로 대처하게 구현되어 있음
 * - But, 보수적으로 정말 사용할 HttpMethod로만 한정해도 됨
 */
public record WhitelistProperties(Map<String, List<String>> values) {

    public List<String> getUrls(HttpMethod method) {
        return Optional.ofNullable(values)
                .map(v -> v.get(method.name()))
                .orElse(List.of());
    }

}
