package com.mini.buting.api.analysis.service;

import com.mini.buting.api.analysis.dto.request.AiFastapiRequest;
import com.mini.buting.api.analysis.dto.response.AiFastapiResponse;
import com.mini.buting.global.exception.BaseException;
import com.mini.buting.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final WebClient fastApiWebClient;

    public AiFastapiResponse analyzeAnimal(AiFastapiRequest req) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("gender", req.gender());

        try {
            ByteArrayResource fileResource = new ByteArrayResource(req.file().getBytes()) {
                @Override
                public String getFilename() {
                    return req.file().getOriginalFilename();
                }
            };

            builder.part("file", fileResource)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
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
}
