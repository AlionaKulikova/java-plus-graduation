package ewm.events.mapper;

import ewm.events.dto.LocationDto;
import ewm.events.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDto locationDto);

    LocationDto toLocationDto(Location location);
}