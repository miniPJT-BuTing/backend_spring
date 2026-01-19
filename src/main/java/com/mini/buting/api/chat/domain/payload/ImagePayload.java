package com.mini.buting.api.chat.domain.payload;

import java.util.List;

public record ImagePayload(List<Image> images) implements Payload {
}
