package com.mini.buting.api.chat.domain;

import java.util.List;

public record ImagePayload(List<Image> images) implements Payload {
}
