package ewm.subscription.mappers;


import ewm.interaction.dto.subscription.SubscriptionDto;
import ewm.subscription.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SubscriptionMapper {
    SubscriptionDto toSubscriptionShortDto(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscription.followerId", source = "followerId")
    @Mapping(target = "subscription.followingId", source = "followingId")
    @Mapping(target = "created", ignore = true)
    Subscription toSubscription(@MappingTarget Subscription subscription, Long followerId, Long followingId);
}
