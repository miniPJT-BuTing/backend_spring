package com.mini.buting.api.university.service;

import com.mini.buting.api.university.domain.UniversityDomain;
import com.mini.buting.api.university.dto.ResolvedUniversity;
import com.mini.buting.api.university.repository.UniversityDomainRepository;
import com.mini.buting.api.university.util.UniversityEmailParser;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * <h2>이메일 도메인 분석 및 대학 식별 Service</h2>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityDomainQueryService {

    private final UniversityDomainRepository universityDomainRepository;
    private final UniversityEmailParser universityEmailParser;

    /**
     * <h3>이메일 주소를 통한 대학 정보 분석</h3>
     * <p>전체 이메일 주소에서 도메인을 추출하고 DB 조회를 통해 대학 정보를 매칭</p>
     *
     * @param email 분석 대상 이메일 주소(Ex: user@pknu.ac.kr)
     * @return {@link ResolvedUniversity} 식별된 대학 정보 DTO
     * @throws BaseException 지원하지 않는 대학 도메인이거나 형식이 잘못된 경우 {@code INVALID_UNIVERSITY_EMAIL} 발생
     */
    public ResolvedUniversity resolveByEmail(String email) {
        String domain = universityEmailParser.extractDomain(email);

        // 대소문자 무시 검색
        UniversityDomain universityDomain = universityDomainRepository.findByDomainIgnoreCase(domain)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL));

        return ResolvedUniversity.from(universityDomain);
    }
}
