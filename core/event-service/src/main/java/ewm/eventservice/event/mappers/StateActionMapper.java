package ewm.eventservice.event.mappers;

import ewm.eventservice.category.mapper.CategoryMapper;
import ewm.eventservice.category.mapper.UserMapper;
import ewm.interaction.dto.event.event.AdminStateAction;
import ewm.interaction.dto.event.event.EventState;
import ewm.interaction.dto.event.event.UserStateAction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, EventMapper.class})
public interface StateActionMapper {
    default EventState toEventState(UserStateAction userStateAction) {
        return switch (userStateAction) {
            case SEND_TO_REVIEW -> EventState.PENDING;
            case CANCEL_REVIEW -> EventState.CANCELED;
        };
    }

    default EventState toEventState(AdminStateAction adminStateAction) {
        return switch (adminStateAction) {
            case PUBLISH_EVENT -> EventState.PUBLISHED;
            case REJECT_EVENT -> EventState.CANCELED;
        };
    }
}