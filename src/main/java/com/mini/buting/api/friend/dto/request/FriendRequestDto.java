package com.mini.buting.api.friend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

/**
 * 친구 요청 관련 DTO 클래스들
 */
public class FriendRequestDto {
    
    /**
     * 친구 요청 보내기 요청 DTO
     */
    @Getter
    public static class SendRequest {
        
        @NotBlank(message = "닉네임은 필수입니다.")
//        @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
        private String targetNickname;
    }
    
    /**
     * 친구 요청 응답 처리 요청 DTO
     */
    @Getter
    public static class ResponseRequest {
        
        private Long friendRequestId;
        private boolean accept; // true: 수락, false: 거절
    }
    
    /**
     * 친구 요청 정보 응답 DTO
     */
    @Getter
    public static class FriendRequestInfo {
        
        private Long id;
        private String status;
        private String requesterNickname;
        private String requesterEmail;
        private String receiverNickname; 
        private String receiverEmail;
        private String createdAt;
        private String respondedAt;
        
        private FriendRequestInfo(Long id, String status, String requesterNickname, 
                                String requesterEmail, String receiverNickname, 
                                String receiverEmail, String createdAt, String respondedAt) {
            this.id = id;
            this.status = status;
            this.requesterNickname = requesterNickname;
            this.requesterEmail = requesterEmail;
            this.receiverNickname = receiverNickname;
            this.receiverEmail = receiverEmail;
            this.createdAt = createdAt;
            this.respondedAt = respondedAt;
        }
        
        public static FriendRequestInfo of(com.mini.buting.api.friend.domain.FriendRequest friendRequest) {
            return new FriendRequestInfo(
                friendRequest.getId(),
                friendRequest.getStatus().name(),
                friendRequest.getRequester().getNickname(),
                friendRequest.getRequester().getFullUniversityEmail(),
                friendRequest.getReceiver().getNickname(),
                friendRequest.getReceiver().getFullUniversityEmail(),
                friendRequest.getCreatedAt().toString(),
                friendRequest.getRespondedAt() != null ? friendRequest.getRespondedAt().toString() : null
            );
        }
    }
    
    /**
     * 친구 요청 목록 조회 응답 DTO
     */
    @Getter
    public static class FriendRequestListResponse {
        
        private List<FriendRequestInfo> sentRequests;     // 내가 보낸 요청들
        private List<FriendRequestInfo> receivedRequests; // 내가 받은 요청들
        
        private FriendRequestListResponse(List<FriendRequestInfo> sentRequests, 
                                        List<FriendRequestInfo> receivedRequests) {
            this.sentRequests = sentRequests;
            this.receivedRequests = receivedRequests;
        }
        
        public static FriendRequestListResponse of(List<com.mini.buting.api.friend.domain.FriendRequest> sent,
                                                 List<com.mini.buting.api.friend.domain.FriendRequest> received) {
            List<FriendRequestInfo> sentInfos = sent.stream()
                .map(FriendRequestInfo::of)
                .toList();
                
            List<FriendRequestInfo> receivedInfos = received.stream()
                .map(FriendRequestInfo::of)
                .toList();
                
            return new FriendRequestListResponse(sentInfos, receivedInfos);
        }
    }
}
