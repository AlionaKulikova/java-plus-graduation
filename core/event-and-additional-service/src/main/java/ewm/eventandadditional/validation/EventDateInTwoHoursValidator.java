package ewm.eventandadditional.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.util.Objects;

public class EventDateInTwoHoursValidator implements ConstraintValidator<EventDateInTwoHours, LocalDateTime> {
    @Override
    public void initialize(EventDateInTwoHours constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDateTime event, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(event)) {
            return true;
        }
        return event.isAfter(LocalDateTime.now().plusHours(2));
    }
}
