package ru.practicum.error.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Class<?> entityClass, Long entityId) {
        super(entityClass.getSimpleName() + " c ID = " + entityId + " не найден.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}