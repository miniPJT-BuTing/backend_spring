package com.mini.buting.api.chat.repository;

import com.mini.buting.api.chat.domain.chatmessage.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, Long> {
    List<ChatMessageDocument> findTop50ByRoomIdOrderByMessageSeqDesc(Long roomId);

    List<ChatMessageDocument> findTop50ByRoomIdAndMessageSeqLessThanOrderByMessageSeqDesc(Long roomId, Long beforeSeq);
}
