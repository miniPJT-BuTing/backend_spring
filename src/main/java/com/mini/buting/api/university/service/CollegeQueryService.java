package com.mini.buting.api.university.service;

import com.mini.buting.api.university.domain.College;
import com.mini.buting.api.university.dto.response.CollegeResponse;
import com.mini.buting.api.university.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollegeQueryService {
    private final CollegeRepository collegeRepository;

    /**
     * 단과대({@link College}) 목록 조회
     *
     * @return 단과대 응답 DTO 목록
     */
    public List<CollegeResponse> getColleges() {
        return collegeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CollegeResponse::from)
                .toList();
    }
}
