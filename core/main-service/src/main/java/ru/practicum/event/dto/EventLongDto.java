package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

@Value
@Builder
public class EventLongDto {
    Long id;
    UserDto initiator;
    String title;
    String annotation;
    String description;
    CategoryDto category;
    Integer participantLimit;
    Integer confirmedRequests;
    Boolean paid;
    Location location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;
    Boolean requestModeration;
    EventState state;
    Long views;
}