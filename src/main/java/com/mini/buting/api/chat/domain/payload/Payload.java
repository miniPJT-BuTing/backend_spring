package com.mini.buting.api.chat.domain.payload;

import com.mini.buting.api.chat.domain.payload.ImagePayload;
import com.mini.buting.api.chat.domain.payload.NoticePayload;
import com.mini.buting.api.chat.domain.payload.TextPayload;
import com.mini.buting.api.chat.domain.payload.VotePayload;

public sealed interface Payload permits TextPayload, ImagePayload, NoticePayload, VotePayload {}
