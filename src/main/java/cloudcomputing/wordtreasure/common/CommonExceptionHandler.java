package cloudcomputing.wordtreasure.common;

import static cloudcomputing.wordtreasure.common.exception.CommonErrorCode.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.internalServerError;

import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(final NoResourceFoundException ex) {
        log.warn("NoResourceFoundException: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(final Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        return internalServerError().body(ApiResponse.fail(INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
