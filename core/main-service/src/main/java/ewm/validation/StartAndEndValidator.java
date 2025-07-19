package ewm.validation;

import ewm.event.dto.PublicEventParam;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.util.Objects;

public class StartAndEndValidator implements ConstraintValidator<StartAndEndValid, PublicEventParam> {
    @Override
    public void initialize(StartAndEndValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(PublicEventParam eventParam, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = eventParam.getRangeStart();
        LocalDateTime end = eventParam.getRangeEnd();
        if (Objects.isNull(start) || Objects.isNull(end)) {
            return true;
        }
        return start.isBefore(end);
    }
}
