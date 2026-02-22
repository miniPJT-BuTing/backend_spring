package com.mini.buting.api.analysis.service;

import com.mini.buting.api.analysis.domain.FaceShape;
import com.mini.buting.api.analysis.dto.request.AiFastapiRequest;
import com.mini.buting.api.analysis.dto.response.AiFastapiResponse;
import com.mini.buting.api.analysis.dto.response.AiResponse;
import com.mini.buting.api.analysis.repository.FaceShapeRepository;
import com.mini.buting.api.member.domain.Gender;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.api.member.repository.MemberRepository;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiService {

    private final WebClient fastApiWebClient;
    private final FaceShapeRepository faceShapeRepository;
    private final MemberRepository memberRepository;

    /**
     * 성별/파일 기반 분석 결과 반환(저장 없음)
     */
    public AiResponse analyzeFaceShape(String gender, MultipartFile file) {
        AiFastapiResponse result = requestFastApiAnalysis(new AiFastapiRequest(gender, file));

        FaceShape faceShape = resolveFaceShape(result.getAnimalType());
        return AiResponse.from(faceShape, result.getDetScore());
    }

    /**
     * 회원 기준으로 분석 수행 후 faceShape를 회원 정보에 저장
     */
    @Transactional
    public AiResponse analyzeFaceShapeAndUpdateMember(Long memberId, MultipartFile file) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        String gender = member.getGender().equals(Gender.M) ? "남자" : "여자";
        AiFastapiResponse result = requestFastApiAnalysis(new AiFastapiRequest(gender, file));
        String animalType = result.getAnimalType();
        log.info("[AI] normalized='{}'", animalType == null ? null : animalType.trim());
        FaceShape faceShape = resolveFaceShape(result.getAnimalType());

        member.updateFaceShape(faceShape);
        return AiResponse.from(faceShape, result.getDetScore());
    }

    /**
     * FastAPI 분석 요청
     */
    private AiFastapiResponse requestFastApiAnalysis(AiFastapiRequest req) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("gender", req.gender());

        try {
            ByteArrayResource fileResource = new ByteArrayResource(req.file().getBytes()) {
                @Override
                public String getFilename() {
                    return req.file().getOriginalFilename();
                }
            };
            builder.part("file", fileResource).contentType(MediaType.APPLICATION_OCTET_STREAM);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.READ_FILE_FAIL);
        }

        return fastApiWebClient.post()
                .uri("/fastapi/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new BaseException(BaseResponseStatus.FAST_API_FAIL)
                                ))
                )
                .bodyToMono(AiFastapiResponse.class)
                .block();
    }

    private FaceShape resolveFaceShape(String animalType) {
        return faceShapeRepository.findByName(animalType.trim())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.UNSUPPORTED_FACE_SHAPE_TYPE));
    }
}
