package com.example.gl_exercise.controller;

import com.example.gl_exercise.exception.UserAlreadyExistsException;
import com.example.gl_exercise.exception.UserNotFoundException;
import com.example.gl_exercise.message.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor global de excepciones para controladores REST.
 */
@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Maneja excepciones no capturadas. Las marco como internal server error porque estas no deben ocurrir.
     *
     * @param e Excepción generada
     * @return Respuesta con error 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception e) {
        log.error(e.getMessage(), e);
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

    /**
     * Maneja errores cuando no se encuentra un usuario.
     *
     * @param e Excepción de usuario no encontrado
     * @return Respuesta con error 404 (Not Found)
     */
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

    /**
     * Maneja errores para cuando se intenta registrar un usuario que ya existe.
     *
     * @param e Excepción de usuario existente
     * @return Respuesta con error 409 (Conflict)
     */
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

    /**
     * Maneja errores de validación de parámetros. Ver las anotaciones de validaciones en el paquete "message" en
     * las clases de Request.
     *
     * @param e Excepción de validación fallida
     * @return Respuesta con error 400 (Bad Request) y detalles de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValid(MethodArgumentNotValidException e) {
        var timestamp = LocalDateTime.now().toString();
        var errs = new ArrayList<ApiErrorResponse.Error>();

        for (var err : e.getBindingResult().getFieldErrors()) {
            errs.add(
                new ApiErrorResponse.Error(
                    timestamp,
                    HttpStatus.BAD_REQUEST.value(),
                    err.getDefaultMessage()
                )
            );
        }

        var body = new ApiErrorResponse(errs);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}
