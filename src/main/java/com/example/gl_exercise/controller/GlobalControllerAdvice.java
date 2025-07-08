package com.example.gl_exercise.controller;

import com.example.gl_exercise.exception.UserAlreadyExistsException;
import com.example.gl_exercise.exception.UserNotFoundException;
import com.example.gl_exercise.message.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception e) {
        var body = new ApiErrorResponse(
            List.of(
                new ApiErrorResponse.Error(
                    LocalDateTime.now().toString(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage()
                )
            )
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userNotFound(UserNotFoundException e) {
        var body = new ApiErrorResponse(
            List.of(new ApiErrorResponse.Error(
                LocalDateTime.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            ))
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> userAlreadyExists(UserAlreadyExistsException e) {
        var body = new ApiErrorResponse(
            List.of(new ApiErrorResponse.Error(
                LocalDateTime.now().toString(),
                HttpStatus.CONFLICT.value(),
                e.getMessage()
            ))
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
