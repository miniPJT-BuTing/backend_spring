package com.mini.buting.api.chat.dto.request;

import java.util.List;

public record VoteCreateRequest (
        String title,
        String description,
        List<VoteOptionRequest> options,
        boolean isMultiple,
        boolean isAnonymous
){
}
