package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, Long> {
}
