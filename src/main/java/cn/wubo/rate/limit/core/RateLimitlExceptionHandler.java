package cn.wubo.rate.limit.core;

import cn.wubo.rate.limit.exception.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RateLimitlExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity handleRateLimitExceeded(RateLimitExceededException ex) {

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ex.getMessage());
    }
}
