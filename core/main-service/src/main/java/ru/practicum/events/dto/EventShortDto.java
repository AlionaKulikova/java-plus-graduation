package ru.practicum.events.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

/**
 * DTO for {@link ru.practicum.events.model.Event}
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