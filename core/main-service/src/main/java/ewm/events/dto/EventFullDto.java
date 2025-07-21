package ewm.events.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ewm.category.dto.CategoryDto;
import ewm.events.model.EventState;
import ewm.user.dto.UserShortDto;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;
    String annotation;
    boolean paid;
    String title;
    String eventDate;
    String description;
    boolean requestModeration;
    int participantLimit;
    String publishedOn;
    String createdOn;
    CategoryDto category;
    UserShortDto initiator;
    LocationDto location;
    EventState state;
    Integer confirmedRequests;
    Long views;
}