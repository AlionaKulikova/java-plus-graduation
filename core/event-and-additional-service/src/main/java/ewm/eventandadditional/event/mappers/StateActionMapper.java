package ewm.eventandadditional.event.mappers;

import ewm.eventandadditional.category.mapper.CategoryMapper;
import ewm.eventandadditional.category.mapper.UserMapper;
import ewm.interaction.dto.eventandadditional.event.AdminStateAction;
import ewm.interaction.dto.eventandadditional.event.EventState;
import ewm.interaction.dto.eventandadditional.event.UserStateAction;
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
