package com.mini.buting.api.university.repository;

import com.mini.buting.api.university.domain.UniversityDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityDomainRepository extends JpaRepository<UniversityDomain, Long> {

    /**
     * <h3>이메일 도메인 문자열을 통한 대학 정보 조회</h3>
     *
     * @param domain 검색할 이메일 도메인 Suffix (Ex: "pusan.ac.kr", "PUSAN.AC.KR")
     * @return {@link Optional}<{@link UniversityDomain}> 매칭되는 대학 도메인 정보
     */
    Optional<UniversityDomain> findByDomainIgnoreCase(String domain);
}
