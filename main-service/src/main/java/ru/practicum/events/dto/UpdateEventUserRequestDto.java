package ru.practicum.events.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventUserRequestDto {
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    @Size(min = 3, max = 120)
    String title;

    @Size(min = 20, max = 2000)
    String annotation;

    @Size(min = 20, max = 7000)
    String description;

    Long category;

    LocationDto location;
    String eventDate;

    Boolean paid;
    @PositiveOrZero
    Integer participantLimit;
    Boolean requestModeration;

    UpdateUserStateAction stateAction;
}
