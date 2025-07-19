package ewm.event.dto;

import ewm.event.model.AdminStateAction;
import ewm.validation.EventDateInOneHour;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest extends UpdateEventRequest {
    AdminStateAction stateAction;
    @EventDateInOneHour
    LocalDateTime eventDate;
}
