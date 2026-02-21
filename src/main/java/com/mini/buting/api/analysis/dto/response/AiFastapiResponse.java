package com.mini.buting.api.analysis.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AiFastapiResponse {

    @JsonProperty("animal_type")
    String animalType;

    @JsonProperty("det_score")
    Double detScore;
}
