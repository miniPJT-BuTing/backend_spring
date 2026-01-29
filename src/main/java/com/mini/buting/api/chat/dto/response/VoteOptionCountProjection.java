package com.mini.buting.api.chat.dto.response;

public interface VoteOptionCountProjection {
    Long getOptionId();
    String getText();
    Integer getOrder();
    Integer getCount();
}
