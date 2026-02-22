package com.mini.buting.api.analysis.controller;

import com.mini.buting.api.analysis.dto.response.AiResponse;
import com.mini.buting.api.analysis.repository.FaceShapeRepository;
import com.mini.buting.api.analysis.service.AiService;
import com.mini.buting.api.member.domain.Gender;
import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.service.MemberService;
import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.security.principal.AuthUser;
import com.mini.buting.global.security.util.AuthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <h2>AI 얼굴 분석 API Controller</h2>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai")
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 얼굴 동물상 분석(비인증/회원가입용)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<AiResponse> analyzeForGuest(
            @Parameter(description = "성별(M/F)", required = true)
            @RequestParam("gender") Gender gender,

            @Parameter(description = "분석할 얼굴 이미지 파일", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file
    ) {
        String genderText = gender.equals(Gender.M) ? "남자" : "여자";
        return BaseResponse.onSuccess(aiService.analyzeFaceShape(genderText, file));
    }

    @Operation(summary = "AI 얼굴 동물상 분석(회원 전용, 결과 저장)")
    @ApiResponse(
            responseCode = "200",
            description = "AI 분석 성공",
            content = @Content(
                    mediaType = "application/json"
            )
    )
    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<AiResponse> getAiAnalysis(
            @AuthenticationPrincipal AuthUser authUser,

            @Parameter(
                    description = "분석할 얼굴 이미지 파일",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            )
            @RequestPart("file")
            MultipartFile file
    ) {
        long memberId = AuthValidator.require(authUser).getId();
        return BaseResponse.onSuccess(aiService.analyzeFaceShapeAndUpdateMember(memberId, file));
    }

}
