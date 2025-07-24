package ewm.subscription.service;

import ewm.interaction.dto.subscription.SubscriptionDto;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.exception.ValidationException;
import ewm.interaction.feign.UserFeignClient;
import ewm.subscription.mapper.SubscriptionMapper;
import ewm.subscription.mapper.UserMapper;
import ewm.subscription.model.Subscription;
import ewm.subscription.repository.SubscriptionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserFeignClient userFeignClient;
    private final SubscriptionMapper subscriptionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public SubscriptionDto subscribe(Long subscriberId, Long subscribedToId) {
        if (subscriberId.equals(subscribedToId)) {
            throw new ConflictException("Пользователь не может подписаться сам на себя");
        }

        Subscription subscription = subscriptionRepository.save(
                subscriptionMapper.toSubscription(new Subscription(), subscriberId, subscribedToId)
        );
        log.info("Подписка уже оформлена");
        SubscriptionDto subscriptionDto = subscriptionMapper.toSubscriptionShortDto(subscription);

        UserDto subscriber;

        try {
            subscriber = userFeignClient.getUsers(List.of(subscriberId), 0, 10).getFirst();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Не найден пользователь с id " + subscriberId);
        }

        UserDto subscribed;

        try {
            subscribed = userFeignClient.getUsers(List.of(subscribedToId), 0, 10).getFirst();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Не найден пользователь с id " + subscribedToId);
        }
        subscriptionDto.setSubscriber(
                userMapper.toUserShortDto(subscriber));
        subscriptionDto.setSubscribed(
                userMapper.toUserShortDto(subscribed));

        return subscriptionDto;
    }

    @Override
    @Transactional
    public void unsubscribe(Long subscriberId, Long subscribedToId) {
        int deleteCount = subscriptionRepository.deleteBySubscribedId(subscribedToId);
        if (deleteCount == 0) {
            throw new ValidationException("Подписка не найдена");
        }
    }

    @Override
    public Set<UserShortDto> getSubscriptions(Long userId, Pageable page) {
        Page<Subscription> subscribed = subscriptionRepository.findBySubscriberId(userId, page);
        List<Long> subscribedIds = subscribed.get()
                .map(Subscription::getSubscribedId)
                .toList();

        return userFeignClient
                .getUsers(subscribedIds, 0, page.getPageSize())
                .stream()
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UserShortDto> getSubscribers(Long userId, Pageable page) {
        Page<Subscription> subscriber = subscriptionRepository.findBySubscriberId(userId, page);
        List<Long> subscribersIds = subscriber.get()
                .map(Subscription::getSubscriberId)
                .toList();

        return userFeignClient
                .getUsers(subscribersIds, 0, page.getPageSize())
                .stream()
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet());
    }
}