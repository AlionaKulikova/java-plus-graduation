package ru.practicum.model;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.practicum.dto.EndpointHit;


@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StatsMapper {
    Hit toModel(EndpointHit createStatsDto);

    EndpointHit toCreationDto(Hit statsModel);
}