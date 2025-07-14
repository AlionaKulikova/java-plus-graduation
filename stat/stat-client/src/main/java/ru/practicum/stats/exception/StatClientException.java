package ru.practicum.stats.exception;

public class StatClientException extends RuntimeException {
    public StatClientException(int statusCode, String message) {
        super("StatusCode is " + statusCode + ".\n" + message);
    }
}