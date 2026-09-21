package com.bookstore.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class WebExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IndexOutOfBoundsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(RuntimeException exception) {
        return Map.of("error", exception.getMessage());
    }
}
