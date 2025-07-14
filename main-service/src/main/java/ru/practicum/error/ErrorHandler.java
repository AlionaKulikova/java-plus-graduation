package ru.practicum.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.ForbiddenOperationException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.error.exception.ResourceNotFoundException;
import ru.practicum.error.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParameter(MissingServletRequestParameterException ex) {
        return new ApiError(
                Collections.singletonList(ex.getMessage()),
                ex.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArgumentValidationException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Некорректный запрос", "Validation failed", errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException e) {
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError(
                "BAD_REQUEST",
                "Некорректный запрос",
                e.getMessage(),
                Collections.singletonList(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Некорректный запрос", "Validation failed", errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.warn("409 {}", e.getMessage(), e);
        return new ApiError("CONFLICT", "Конфликт данных", e.getMessage(), Collections.singletonList(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleForbiddenOperationException(ForbiddenOperationException e) {
        log.warn("409 {}", e.getMessage(), e);
        return new ApiError("CONFLICT", "Нарушение условий операции", e.getMessage(), Collections.singletonList(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.warn("404 {}", e.getMessage(), e);
        return new ApiError("NOT_FOUND", "Объект не найден", e.getMessage(), Collections.singletonList(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("404 {}", e.getMessage(), e);
        return new ApiError("NOT_FOUND", "Объект не найден", e.getMessage(), Collections.singletonList(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        log.error("500 {}", e.getMessage(), e);
        return new ApiError("INTERNAL_SERVER_ERROR", "Ошибка сервера", e.getMessage(), Collections.singletonList(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>("Ошибка десериализации тела запроса: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}