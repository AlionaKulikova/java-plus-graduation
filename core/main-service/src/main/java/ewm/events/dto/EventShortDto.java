package ewm.events.dto;

import ewm.events.model.Event;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ewm.category.dto.CategoryDto;
import ewm.user.dto.UserShortDto;

/**
 * DTO for {@link Event}
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    Long id;
    String annotation;
    boolean paid;

    String title;
    String eventDate;

    CategoryDto category;
    UserShortDto initiator;

    Long confirmedRequests;
    Long views;

}