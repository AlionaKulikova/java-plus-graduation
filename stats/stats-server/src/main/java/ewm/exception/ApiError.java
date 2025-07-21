package ewm.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    String error;
    String description;

    public ApiError(String error, String description) {
        this.error = error;
        this.description = description;
    }

    public ApiError(HttpStatus status, String error, String description, String stackTrace) {
        this.error = error;
        this.description = description;
    }
}
