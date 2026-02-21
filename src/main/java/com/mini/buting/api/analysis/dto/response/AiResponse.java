package com.mini.buting.api.analysis.dto.response;

import com.mini.buting.api.analysis.domain.FaceShape;

public record AiResponse(
        Long faceShapeId,
        String name,
        String nickname,
        String description,
        String image,
        Double percentage
) {

    public static AiResponse from(FaceShape faceShape, Double percentage){
        return new AiResponse(
                faceShape.getId(),
                faceShape.getName(),
                faceShape.getNickname(),
                faceShape.getDescription(),
                faceShape.getImage(),
                percentage
        );
    }
}
