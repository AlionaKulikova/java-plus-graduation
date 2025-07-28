package ewm.subscription.mapper;

import ewm.interaction.dto.subscription.SubscriptionDto;
import ewm.subscription.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SubscriptionMapper {
    SubscriptionDto toSubscriptionShortDto(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscription.subscriberId", source = "subscriberId")
    @Mapping(target = "subscription.subscribedId", source = "subscribedId")
    @Mapping(target = "created", ignore = true)
    Subscription toSubscription(@MappingTarget Subscription subscription, Long subscriberId, Long subscribedId);
}