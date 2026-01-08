package com.mini.buting.api.chat.domain.chatroom;

import com.mini.buting.api.member.domain.Member;
import com.mini.buting.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room_member",
        indexes = {
                @Index(name = "idx_user_room", columnList = "user_id, room_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseTimeEntity {

    @EmbeddedId
    private ChatRoomMemberId id;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, updatable = false)
    private ChatRoom chatRoom;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "last_read_seq", nullable = false)
    private Long lastReadSeq;

    @Builder
    private ChatRoomMember(ChatRoom chatRoom, Member member, Long lastReadSeq) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.id = new ChatRoomMemberId(chatRoom.getRoomId(), member.getId());
        this.lastReadSeq = (lastReadSeq == null ? 0L : lastReadSeq);
    }

    public void advanceLastReadSeq(Long newSeq) {
        if (newSeq == null) return;
        if (this.lastReadSeq == null) this.lastReadSeq = 0L;
        if (newSeq > this.lastReadSeq) this.lastReadSeq = newSeq;
    }
}
