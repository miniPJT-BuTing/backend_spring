package com.mini.buting.api.university.dto;

import com.mini.buting.api.university.domain.UniversityDomain;

/**
 * <h2>도메인 매칭 결과 DTO</h2>
 * <p>입력된 이메일 주소에서 추출된 도메인을 바탕으로 식별된 대학 정보를 담음</p>
 *
 * @param universityDomainId 대학 도메인 고유 식별자 (PK)
 * @param universityId       연결된 대학 본체 고유 식별자 (FK)
 * @param universityName     식별된 대학의 국문 명칭 (Ex: 부산대학교)
 * @param domain             분석에 사용된 이메일 도메인 문자열 (Ex: pnu.ac.kr)
 * @implNote 이메일 인증 완료 후 회원가입 폼으로 대학 정보를 전달하거나,
 * 가입 프로세스에서 대학 식별자를 확정할 때 사용
 */
public record ResolvedUniversity(
        Long universityDomainId,
        Long universityId,
        String universityName,
        String domain
) {
    public static ResolvedUniversity from(UniversityDomain universityDomain) {
        return new ResolvedUniversity(
                universityDomain.getId(),
                universityDomain.getUniversity().getId(),
                universityDomain.getUniversity().getName(),
                universityDomain.getDomain()
        );
    }
}
