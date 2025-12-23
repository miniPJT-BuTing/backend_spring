package com.mini.buting.global.exception;

import com.mini.buting.global.response.BaseResponse;
import com.mini.buting.global.response.BaseResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;


/**
 * Controller 단에서 발생하는 예외를 처리
 */
@RestControllerAdvice
public class BaseExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(BaseExceptionHandler.class);

    /* 비즈니스 예외 */
    @ExceptionHandler(BaseException.class)
    protected BaseResponse<Void> baseError(BaseException e) {
        writeLog(e.getStatus(), e);
        return BaseResponse.onFailure(e.getStatus());
    }

    /* @Valid 유효성 검사 예외 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected BaseResponse<Void> handleValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getFieldErrors().get(0).getDefaultMessage();
        writeLog(BaseResponseStatus.INVALID_REQUEST, e, errorMessage);
        return BaseResponse.onFailure(BaseResponseStatus.INVALID_REQUEST, errorMessage);
    }

    /* 파일 용량 초과 */
    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public BaseResponse<Void> handleMaxUploadSize(Exception e) {
        writeLog(BaseResponseStatus.PAYLOAD_TOO_LARGE, e);
        return BaseResponse.onFailure(BaseResponseStatus.PAYLOAD_TOO_LARGE);
    }

    /* 서버 내부 오류 */
    @ExceptionHandler(Exception.class)
    protected BaseResponse<Void> handleRuntimeException(Exception e) {
        writeLog(BaseResponseStatus.INTERNAL_SERVER_ERROR, e);
        return BaseResponse.onFailure(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalState(IllegalStateException ex) {
        String msg = ex.getMessage();
        if (msg != null && msg.contains("exceeds its maximum permitted size")) {
            log.error("Multipart size exceeded (IllegalState): {}", msg);
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(BaseResponse.onFailure(BaseResponseStatus.PAYLOAD_TOO_LARGE));
        }
        throw ex; // 업로드 초과 케이스가 아니면 기존 흐름 유지
    }

    private void writeLog(BaseResponseStatus status, Exception e) {
        log.error("[{}] Status: {} | Message: {}",
                e.getClass().getSimpleName(), status, status.getMessage(), e);
    }

    private void writeLog(BaseResponseStatus status, Exception e, String customMessage) {
        log.error("[{}] Status: {} | CustomMessage: {}",
                e.getClass().getSimpleName(), status, customMessage);
    }
}
