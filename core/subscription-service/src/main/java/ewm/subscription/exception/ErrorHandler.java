package ewm.subscription.exception;

import ewm.interaction.exception.ApiError;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(Exception exception) {
        String cause = "Ошибка при валидации данных";
        log.info("{}: {}", cause, exception.getMessage());
        return ApiError.builder()
                .message(exception.getMessage())
                .reason(cause)
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({ConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(Exception exception) {
        String cause = "Нарушение целостности данных";
        log.info("{}: {}", cause, exception.getMessage());
        return ApiError.builder()
                .message(exception.getMessage())
                .reason(cause)
                .status(HttpStatus.CONFLICT.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException exception) {
        String cause = "Ошибка при поиске данных";
        log.info("{}: {}", cause, exception.getMessage());
        return ApiError.builder()
                .message(exception.getMessage())
                .reason(cause)
                .status(HttpStatus.NOT_FOUND.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String errors = stringWriter.toString();
        String cause = "Внутренняя ошибка сервера";
        log.info("{}: {}", cause, exception.getMessage());
        return ApiError.builder()
                .errors(errors)
                .message(exception.getMessage())
                .reason(cause)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
