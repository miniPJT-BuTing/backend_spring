package com.mini.buting.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static com.mini.buting.global.response.BaseResponseStatus.SUCCESS;

public record BaseResponse<T>(
        HttpStatusCode httpStatus,
        Boolean isSuccess,
        String message,
        int code,
        T result
) {
    /* 성공 응답 */
    public static BaseResponse<Void> onSuccess() {
        return new BaseResponse<>(HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), null);
    }

    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>(HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), result);
    }

    /* 실패 응답 */
    public static <T> BaseResponse<T> onFailure(BaseResponseStatus status) {
        return new BaseResponse<>(status.getHttpStatusCode(), status.isSuccess(), status.getMessage(), status.getCode(), null);
    }

    public static <T> BaseResponse<T> onFailure(BaseResponseStatus status, String message) {
        return new BaseResponse<>(status.getHttpStatusCode(), status.isSuccess(), message, status.getCode(), null);
    }
}
