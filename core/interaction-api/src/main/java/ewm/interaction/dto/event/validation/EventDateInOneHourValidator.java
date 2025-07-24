package ewm.interaction.dto.event.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.util.Objects;

public class EventDateInOneHourValidator implements ConstraintValidator<EventDateInOneHour, LocalDateTime> {
    @Override
    public void initialize(EventDateInOneHour constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDateTime event, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(event)) {
            return true;
        }
        return event.isAfter(LocalDateTime.now().plusHours(1));
    }
}
