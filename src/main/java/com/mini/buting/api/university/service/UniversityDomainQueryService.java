package com.mini.buting.api.university.service;

import com.mini.buting.api.university.domain.UniversityDomain;
import com.mini.buting.api.university.dto.ResolvedUniversity;
import com.mini.buting.api.university.repository.UniversityDomainRepository;
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

    /**
     * <h3>이메일 주소를 통한 대학 정보 분석</h3>
     * <p>전체 이메일 주소에서 도메인을 추출하고 DB 조회를 통해 대학 정보를 매칭</p>
     *
     * @param email 분석 대상 이메일 주소(Ex: user@pknu.ac.kr)
     * @return {@link ResolvedUniversity} 식별된 대학 정보 DTO
     * @throws BaseException 지원하지 않는 대학 도메인이거나 형식이 잘못된 경우 {@code INVALID_UNIVERSITY_EMAIL} 발생
     */
    public ResolvedUniversity resolveByEmail(String email) {
        String domain = extractDomain(email);

        // 대소문자 무시 검색
        UniversityDomain universityDomain = universityDomainRepository.findByDomainIgnoreCase(domain)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL));

        return ResolvedUniversity.from(universityDomain);
    }

    /**
     * <h3>이메일 도메인 파트 추출</h3>
     * <p>이메일 문자열에서 '@' 기호를 기준으로 도메인 부분만 분리하여 소문자로 정규화</p>
     * <ul>
     *     <li>앞뒤 공백 제거(trim), 소문자 변환</li>
     *     <li>'@' 기호 존재 여부 및 위치 유효성 검증</li>
     * </ul>
     *
     * @param email 원본 이메일 문자열
     * @return 소문자로 정규화된 도메인 문자열(Ex: pknu.ac.kr)
     */
    private String extractDomain(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL);
        }

        // 소문자 및 공백 처리
        String normalized = email.trim().toLowerCase(Locale.ROOT);

        // '@' 위치 기반 도메인 분리
        int atIndex = normalized.lastIndexOf('@');
        if (atIndex < 1 || atIndex == normalized.length() - 1) {
            // @가 없거나, 맨 앞에 있거나, 맨 뒤에 있는 경우 탈락
            throw new BaseException(BaseResponseStatus.INVALID_UNIVERSITY_EMAIL);
        }

        return normalized.substring(atIndex + 1);
    }
}
