package ewm.request.mapper;

import ewm.event.model.Event;
import ewm.request.dto.ParticipationRequestDto;
import ewm.request.model.ParticipationRequest;
import ewm.user.mappers.UserMapper;
import ewm.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {Event.class, UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RequestMapper {
    @Mapping(target = "requester", source = "participationRequest.requester.id")
    @Mapping(target = "event", source = "participationRequest.event.id")
    ParticipationRequestDto toParticipantRequestDto(ParticipationRequest participationRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", source = "event")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "requester", source = "requester")
    ParticipationRequest toParticipationRequest(Event event, User requester);
}
