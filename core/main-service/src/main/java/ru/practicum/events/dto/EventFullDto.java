package ru.practicum.events.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.events.model.EventState;
import ru.practicum.user.dto.UserShortDto;

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