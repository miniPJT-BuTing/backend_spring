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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai")
public class AiController {

    private final AiService aiService;
    private final MemberService memberService;
    private final FaceShapeRepository faceShapeRepository;

    @PostMapping
    public BaseResponse<AiResponse> getAiAnalysis(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @RequestPart("file") MultipartFile file
            ){
        MemberProfileResponse memberProfile = memberService.getMemberProfile(userId);
        String gender = memberProfile.getGender().equals(Gender.M) ? "남자" : "여자";

        AiFastapiResponse result = aiService.analyzeAnimal(new AiFastapiRequest(gender, file));

        FaceShape faceShape = faceShapeRepository.findByName(result.getAnimalType());

        AiResponse aiResponse = AiResponse.from(faceShape, result.getDetScore());

        return BaseResponse.onSuccess(aiResponse);
    }

}
