package ru.practicum.request.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
public interface RequestsMapper {
    RequestsMapper REQUESTS_MAPPER = Mappers.getMapper(RequestsMapper.class);

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);
}