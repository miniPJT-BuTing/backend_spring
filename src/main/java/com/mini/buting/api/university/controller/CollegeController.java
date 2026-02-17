package com.mini.buting.api.university.controller;

import com.mini.buting.api.university.dto.response.CollegeResponse;
import com.mini.buting.api.university.service.CollegeQueryService;
import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <h2>단과대(College) 컨트롤러</h2>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/colleges")
@Tag(name = "University", description = "대학/단과대 관련 API")
public class CollegeController {
    private final CollegeQueryService collegeQueryService;

    @Operation(summary = "단과대 목록 조회 API", description = "선택 가능한 단과대 목록을 반환")
    @GetMapping()
    public BaseResponse<List<CollegeResponse>> getColleges() {
        return BaseResponse.onSuccess(collegeQueryService.getColleges());
    }
}
