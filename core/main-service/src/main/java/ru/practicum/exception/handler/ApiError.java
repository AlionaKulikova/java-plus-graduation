package ru.practicum.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ApiError {

    private HttpStatus status;
    private String reason;
    private String message;
    private List<String> errors;
    private String timestamp;
}
