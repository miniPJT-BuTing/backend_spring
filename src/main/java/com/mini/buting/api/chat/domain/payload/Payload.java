package com.mini.buting.api.chat.domain.payload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "payloadType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextPayload.class, name = "TEXT"),
        @JsonSubTypes.Type(value = ImagePayload.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = VotePayload.class, name = "VOTE"),
        @JsonSubTypes.Type(value = NoticePayload.class, name = "NOTICE")
})
public sealed interface Payload permits ImagePayload, NoticePayload, TextPayload, VotePayload, WelcomePayload {}
