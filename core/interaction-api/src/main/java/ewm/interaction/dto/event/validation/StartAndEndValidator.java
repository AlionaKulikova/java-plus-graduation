package ewm.interaction.dto.event.validation;

import ewm.interaction.dto.event.event.ParamsEventPublic;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.util.Objects;

public class StartAndEndValidator implements ConstraintValidator<StartAndEndValid, ParamsEventPublic> {
    @Override
    public void initialize(StartAndEndValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(ParamsEventPublic eventParam, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = eventParam.getRangeStart();
        LocalDateTime end = eventParam.getRangeEnd();
        if (Objects.isNull(start) || Objects.isNull(end)) {
            return true;
        }
        return start.isBefore(end);
    }
}
