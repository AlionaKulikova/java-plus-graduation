package ewm.subscriptions.mappers;

import ewm.subscriptions.dto.SubscriptionDto;
import ewm.subscriptions.model.Subscription;
import ewm.user.mappers.UserMapper;
import ewm.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SubscriptionMapper {
    SubscriptionDto toSubscriptionShortDto(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscription.follower", source = "follower")
    @Mapping(target = "subscription.following", source = "following")
    Subscription toSubscription(@MappingTarget Subscription subscription, User follower, User following);
}
