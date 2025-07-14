package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.practicum.StatDto;
import ru.practicum.model.Stat;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatMapper {
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.parse(statDto.getTimestamp(), " +
            "java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\")))")
    Stat toEntity(StatDto statDto);

    @Mapping(source = "timestamp", target = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    StatDto toDto(Stat entity);
}