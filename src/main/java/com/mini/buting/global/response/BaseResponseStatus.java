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
    MATCH_REQUEST_EXPIRED(HttpStatus.BAD_REQUEST, false, 4002, "만료된 매칭 요청입니다.");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}
