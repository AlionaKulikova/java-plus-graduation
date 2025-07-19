package ru.practicum.exception.handler;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @ExceptionHandler({MissingRequestHeaderException.class, MethodArgumentNotValidException.class,
            BadRequestException.class, ConstraintViolationException.class, MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(final Exception e) {
        log.error("{} - Status: {}, Description: {}, Timestamp: {}",
                "Bad Request", HttpStatus.BAD_REQUEST, e.getMessage(), LocalDateTime.now());

        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.name(), e.getMessage(),
                "Bad Request", LocalDateTime.now().format(FORMATTER)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInternalServerError(final Exception e) {
        log.error("SERVER ERROR - Status: {}, Description: {}, Timestamp: {}", HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage(), LocalDateTime.now(), e);


        return new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), e.getMessage(),
                "Internal Server Error " + e, LocalDateTime.now().format(FORMATTER)), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class, NotUniqueException.class})
    public ResponseEntity<ErrorResponse> handleConflict(final Exception e) {
        log.error("{} - Status: {}, Description: {}, Timestamp: {}",
                "CONFLICT", HttpStatus.CONFLICT, e.getMessage(), LocalDateTime.now());

        return new ResponseEntity<>(new ErrorResponse(HttpStatus.CONFLICT.name(), e.getMessage(),
                "Data Integrity constraint violation occurred", LocalDateTime.now().format(FORMATTER)),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> errorNotFound(final RuntimeException e) {
        log.error("{} - Status: {}, Description: {}, Timestamp: {}",
                "NOT FOUND", HttpStatus.NOT_FOUND, e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.name(), "The required object was not found.", e.getMessage(),
                LocalDateTime.now().format(FORMATTER)), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError errorConflictData(DataConflictRequest e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return new ApiError(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.",
                e.getMessage(), List.of(stackTrace), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError errorInvalidRequestException(InvalidRequestException e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return new ApiError(HttpStatus.BAD_REQUEST, "Integrity constraint has been violated.", e.getMessage(),
                List.of(stackTrace), LocalDateTime.now().format(FORMATTER));
    }

}