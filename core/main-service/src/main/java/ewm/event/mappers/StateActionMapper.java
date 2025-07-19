package ewm.event.mappers;

import ewm.category.mapper.CategoryMapper;
import ewm.event.model.AdminStateAction;
import ewm.event.model.EventState;
import ewm.event.model.UserStateAction;
import ewm.user.mappers.UserMapper;
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
