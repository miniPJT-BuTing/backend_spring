package com.mini.buting.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum BaseResponseStatus {
    /**
     * 2xx: Success (성공)
     */
    SUCCESS(HttpStatus.OK, true, 200, "요청에 성공하였습니다."),

    /**
     * 4xx: Client Error (클라이언트 에러)
     */
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, false, 400, "올바르지 않은 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "요청하신 정보를 찾을 수 없습니다."),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, false, 413, "파일 업로드 용량 초과입니다. 파일 당 요청 합계 제한을 확인하세요."),

    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, false, 401, "인증이 필요한 요청입니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, false, 403, "접근 권한이 없습니다."),

    /**
     * 5xx: Sever Error (서버 에러)
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "서버에서 예기치 않은 오류가 발생했습니다."),

    /**
     * Service Related Custom Errors
     */

    /**
     * 2000: Member Service 관련 에러
     */
    MEMBER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, false, 2001, "이미 탈퇴 처리된 회원입니다."),
    SOCIAL_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, false, 2002, "지원하지 않는 소셜 로그인 유형입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, false, 2003, "존재하지 않는 회원입니다."),
    MEMBER_DELETED_USER(HttpStatus.FORBIDDEN, false, 2004, "탈퇴한 사용자의 프로필은 조회할 수 없습니다."),

    /**
     * Friend Related Errors (친구 관련 에러)
     */
    // 친구 요청 관련 (2100번대)
    FRIEND_REQUEST_NOT_PENDING(HttpStatus.BAD_REQUEST, false, 2100, "처리할 수 없는 친구 요청입니다."),
    FRIEND_REQUEST_EXPIRED(HttpStatus.BAD_REQUEST, false, 2101, "만료된 친구 요청입니다."),
    FRIEND_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, false, 2102, "이미 친구 요청이 존재합니다."),
    FRIEND_REQUEST_TO_SELF(HttpStatus.BAD_REQUEST, false, 2103, "자기 자신에게는 친구 요청을 보낼 수 없습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, false, 2104, "친구 요청을 찾을 수 없습니다."),

    // 친구 관계 관련 (2200번대)
    ALREADY_FRIENDS(HttpStatus.CONFLICT, false, 2200, "이미 친구 관계입니다."),
    NOT_FRIENDS(HttpStatus.BAD_REQUEST, false, 2201, "친구 관계가 아닙니다."),
    FRIEND_BLOCKED(HttpStatus.FORBIDDEN, false, 2202, "차단된 사용자입니다."),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, false, 2203, "친구를 찾을 수 없습니다."),

    // 권한 관련 (2300번대)
    NO_PERMISSION_TO_RESPOND(HttpStatus.FORBIDDEN, false, 2300, "친구 요청에 응답할 권한이 없습니다."),
    NO_PERMISSION_TO_CANCEL(HttpStatus.FORBIDDEN, false, 2301, "친구 요청을 취소할 권한이 없습니다."),

    // 검색 관련 (2400번대)
    INVALID_SEARCH_QUERY(HttpStatus.BAD_REQUEST, false, 2400, "유효하지 않은 검색어입니다."),

    /**
     * 3000: Team Service 관련 에러
     */
    TEAM_FULL(HttpStatus.BAD_REQUEST, false, 3001, "팀 정원이 가득 찼습니다."),
    ALREADY_TEAM_MEMBER(HttpStatus.BAD_REQUEST, false, 3002, "이미 해당 팀의 멤버입니다."),
    NOT_TEAM_MEMBER(HttpStatus.BAD_REQUEST, false, 3003, "해당 팀의 멤버가 아닙니다."),

    /**
     * 4000: match request 관련 에러
     */
    MATCH_REQUEST_NOT_PENDING(HttpStatus.BAD_REQUEST, false, 4001, "대기 중인 매칭 요청이 아닙니다."),
    MATCH_REQUEST_EXPIRED(HttpStatus.BAD_REQUEST, false, 4002, "만료된 매칭 요청입니다."),
    MATCH_REQUEST_NOT_ACCEPTED(HttpStatus.BAD_REQUEST, false, 4001, "수락 된 매칭 요청이 아닙니다."),
    /**
     * 4100: MBTI 관련 에러
     */
    INVALID_MBTI_FORMAT(HttpStatus.BAD_REQUEST, false, 4100, "올바르지 않은 MBTI 형식입니다. 4글자 알파벳으로 입력해주세요."),
    MBTI_REQUIRED(HttpStatus.BAD_REQUEST, false, 4101, "MBTI는 필수 입력 항목입니다."),

    /**
     * 4200: 성격 키워드 관련 에러
     */
    INVALID_PERSONALITY_COUNT(HttpStatus.BAD_REQUEST, false, 4200, "성격 키워드는 정확히 3개를 선택해야 합니다."),
    DUPLICATE_PERSONALITY_TYPES(HttpStatus.BAD_REQUEST, false, 4201, "중복된 성격 키워드는 선택할 수 없습니다."),
    INVALID_PERSONALITY_TYPE(HttpStatus.BAD_REQUEST, false, 4202, "유효하지 않은 성격 키워드입니다."),
    PERSONALITY_REQUIRED(HttpStatus.BAD_REQUEST, false, 4203, "성격 키워드는 필수 입력 항목입니다."),

    /**
     * 4300: 성격 키워드 관련 에러
     */
    UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, false, 4301, "존재하지 않는 대학입니다."),
    COLLEGE_NOT_FOUND(HttpStatus.NOT_FOUND, false, 4302, "존재하지 않는 단과대입니다."),
    INVALID_UNIVERSITY_EMAIL(HttpStatus.BAD_REQUEST, false, 4303, "올바르지 않은 대학 이메일입니다."),


    /**
     * 5000: 채팅방 관련 에러
     */
    CHATROOM_NOT_EXISTS(HttpStatus.NOT_FOUND, false, 5001, "존재하지 않는 채팅방입니다."),
    NOT_CHATROOM_MEMBER(HttpStatus.NOT_FOUND, false, 5002, "접근할 수 없는 채팅방입니다." ),
    MESSAGE_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 5003, "메시지 전송에 실패했습니다." ),
    CHATROOM_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, false, 5004, "이미 존재하는 채팅방입니다.");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;

}
