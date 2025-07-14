package ru.practicum.events.mapper;

import org.mapstruct.*;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequestDto;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {

    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "eventDate", source = "newEventDto.eventDate", dateFormat = DATE_FORMAT)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "paid", source = "newEventDto.paid", defaultValue = "false")
    @Mapping(target = "participantLimit", source = "newEventDto.participantLimit", defaultValue = "0")
    @Mapping(target = "requestModeration", source = "newEventDto.requestModeration", defaultValue = "true")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", constant = "0")
    @Mapping(target = "views", ignore = true)
    Event toEvent(NewEventDto newEventDto, User initiator, Category category, Location location);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true) // Состояние обновляется отдельно
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "eventDate", source = "updateRequest.eventDate", dateFormat = DATE_FORMAT)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    void updateEventFromDto(UpdateEventUserRequestDto updateRequest, Category category, Location location, @MappingTarget Event event);

    @Mapping(target = "createdOn", dateFormat = DATE_FORMAT)
    @Mapping(target = "publishedOn", dateFormat = DATE_FORMAT)
    @Mapping(target = "eventDate", dateFormat = DATE_FORMAT)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "eventDate", dateFormat = DATE_FORMAT)
    EventShortDto toEventShortDto(Event event);
}