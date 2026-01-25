package com.mini.buting.global.security.property;

import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <h2>인증 제외(Permit-All) 화이트리스트 관리 레코드</h2>
 *
 * <p>Security Filter를 거치지 않고 전체 공개되는 API 경로 목록을 HTTP 메서드별로 관리하여 불필요한 인증 절차를 무시하고 공용 자원 접근성을 높임
 * 동적 맵 바인딩을 통해 {@code yaml}의 내용과 상관 없이 유연한 메서드 확장이 가능이 가능하도록 구현됨</p>
 *
 * @param values HTTP 메서드명을 Key로 하고, URL 패턴 리스트를 값으로 가지는 맵
 */
public record WhitelistProperties(Map<String, List<String>> values) {

    /**
     * 요청된 HTTP 메서드에 해당하는 화이트리스트 URL 목록을 조회
     *
     * @param method 조회하고자 하는 HTTP 메서드
     * @return 해당 메서드에 할당된 URL 패턴 리스트. 설정이 없을 경우 빈 리스트를 리턴
     */
    public List<String> getUrls(HttpMethod method) {
        return Optional.ofNullable(values)
                .map(v -> v.get(method.name()))
                .orElse(List.of());
    }

}
