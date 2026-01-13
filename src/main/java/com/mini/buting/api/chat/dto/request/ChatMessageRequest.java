package com.mini.buting.api.chat.dto.request;

import com.mini.buting.api.chat.domain.MessageType;
import com.mini.buting.api.chat.domain.payload.Payload;
public record ChatMessageRequest (
     String roomId,
     MessageType type,
     Payload payload
){}
