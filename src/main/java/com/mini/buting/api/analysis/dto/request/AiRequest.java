package com.mini.buting.api.analysis.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record AiRequest(
        MultipartFile file
) {
}
