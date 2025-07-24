package ewm.request.mapper;


import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.dto.request.ParticipationRequestDto;
import ewm.request.model.ParticipationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {Event.class, UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RequestMapper {
    @Mapping(target = "requester", source = "participationRequest.requesterId")
    @Mapping(target = "event", source = "participationRequest.eventId")
    ParticipationRequestDto toParticipantRequestDto(ParticipationRequest participationRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "requesterId", source = "requester.id")
    ParticipationRequest toParticipationRequest(Event event, User requester);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", source = "eventFullDto.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "requesterId", source = "requester.id")
    ParticipationRequest toParticipationRequest(EventFullDto eventFullDto, User requester);
}
