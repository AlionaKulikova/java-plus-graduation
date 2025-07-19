package ru.practicum.subscriptions.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.subscriptions.dto.SubscriptionDto;
import ru.practicum.subscriptions.model.Subscription;
import ru.practicum.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SubscriptionMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subscriber", source = "subscription.subscriber")
    @Mapping(target = "subscribedTo", source = "subscription.subscribedTo")
    @Mapping(target = "created", source = "subscription.created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    SubscriptionDto toDto(Subscription subscription);
}
