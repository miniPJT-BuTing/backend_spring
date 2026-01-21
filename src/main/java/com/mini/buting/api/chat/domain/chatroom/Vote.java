package com.mini.buting.api.chat.domain.chatroom;

import com.mini.buting.api.chat.dto.request.VoteCreateRequest;
import com.mini.buting.api.member.domain.Member;
import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "vote",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vote_message", columnNames = {"message_id"})
        },
        indexes = {
                @Index(name = "idx_vote_room", columnList = "room_id")
        }
)
public class Vote extends BaseTimeEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, updatable = false)
    private ChatRoom chatRoom;

    @Column(name = "message_id", nullable = true, updatable = true, length = 100)
    private String messageId;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "is_multiple", nullable = false)
    private boolean multiple;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VoteStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private Member createdBy;

    public static Vote create(Long voteId, ChatRoom room, Member creator, String messageId,
                              VoteCreateRequest request) {
        Vote v = new Vote();
        v.id = voteId;
        v.chatRoom = room;
        v.createdBy = creator;
        v.messageId = messageId;
        v.title = request.title();
        v.description = request.description();
        v.multiple = request.isMultiple();
        v.anonymous = request.isAnonymous();
        v.status = VoteStatus.OPEN;
        return v;
    }

    public void close() {
        this.status = VoteStatus.CLOSED;
    }

    public void attachMessage(String messageId){
        this.messageId = messageId;
    }

}
