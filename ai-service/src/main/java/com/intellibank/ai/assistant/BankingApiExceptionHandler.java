package com.intellibank.ai.assistant;

import com.intellibank.ai.banking.BankingApiException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BankingApiExceptionHandler {
    @ExceptionHandler(BankingApiException.class)
    public ResponseEntity<Map<String, Object>> backendFailure(BankingApiException exception) {
        int status = exception.getStatus();
        HttpStatusCode httpStatus = status >= 400 && status <= 599
                ? HttpStatusCode.valueOf(status)
                : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(httpStatus)
                .body(Map.of("error", exception.getMessage(), "source", "banking-backend"));
    }
}
