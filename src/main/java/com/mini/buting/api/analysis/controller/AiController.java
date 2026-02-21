package com.mini.buting.api.analysis.controller;

import com.mini.buting.api.analysis.domain.FaceShape;
import com.mini.buting.api.analysis.dto.request.AiFastapiRequest;

import com.mini.buting.api.analysis.dto.response.AiFastapiResponse;
import com.mini.buting.api.analysis.dto.response.AiResponse;
import com.mini.buting.api.analysis.repository.FaceShapeRepository;
import com.mini.buting.api.analysis.service.AiService;
import com.mini.buting.api.member.domain.Gender;
import com.mini.buting.api.member.dto.MemberProfileResponse;
import com.mini.buting.api.member.service.MemberService;
import com.mini.buting.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai")
public class AiController {

    private final AiService aiService;
    private final MemberService memberService;
    private final FaceShapeRepository faceShapeRepository;

    @Operation(
            summary = "AI 얼굴 동물상 분석",
            description = "이미지 파일을 업로드하면 AI 분석을 통해 동물상 결과를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "AI 분석 성공",
            content = @Content(
                    mediaType = "application/json"
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<AiResponse> getAiAnalysis(

            @Parameter(
                    description = "사용자 ID",
                    example = "1"
            )
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1")
            Long userId,

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

        MemberProfileResponse memberProfile = memberService.getMemberProfile(userId);
        String gender = memberProfile.getGender().equals(Gender.M) ? "남자" : "여자";

        AiFastapiResponse result = aiService.analyzeAnimal(new AiFastapiRequest(gender, file));

        FaceShape faceShape = faceShapeRepository.findByName(result.getAnimalType());
        AiResponse aiResponse = AiResponse.from(faceShape, result.getDetScore());

        return BaseResponse.onSuccess(aiResponse);
    }

}
