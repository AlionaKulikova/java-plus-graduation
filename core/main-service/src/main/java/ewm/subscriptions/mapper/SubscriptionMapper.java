package ewm.subscriptions.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.subscriptions.dto.SubscriptionDto;
import ewm.subscriptions.model.Subscription;
import ewm.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SubscriptionMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subscriber", source = "subscription.subscriber")
    @Mapping(target = "subscribedTo", source = "subscription.subscribedTo")
    @Mapping(target = "created", source = "subscription.created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    SubscriptionDto toDto(Subscription subscription);
}
