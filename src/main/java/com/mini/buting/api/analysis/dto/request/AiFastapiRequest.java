package com.mini.buting.api.analysis.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record AiFastapiRequest (

    String gender,
    MultipartFile file
){}
