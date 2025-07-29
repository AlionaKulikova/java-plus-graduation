package ewm.subscription.service;

import ewm.interaction.dto.subscription.SubscriptionDto;
import ewm.interaction.dto.user.UserDto;
import ewm.interaction.dto.user.UserShortDto;
import ewm.interaction.exception.ConflictException;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.exception.ValidationException;
import ewm.interaction.feign.UserFeignClient;
import ewm.subscription.mappers.SubscriptionMapper;
import ewm.subscription.mappers.UserMapper;
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
    final SubscriptionRepository subscriptionRepository;
    final UserFeignClient userFeignClient;
    final SubscriptionMapper subscriptionMapper;
    final UserMapper userMapper;

    @Override
    public Set<UserShortDto> findFollowing(long userId, Pageable page) {
        log.info("Получение подписок текущего пользователя с id = {}", userId);

        Page<Subscription> following = subscriptionRepository.findByFollowerId(userId, page);

        List<Long> followingIds = following.get()
                .map(Subscription::getFollowingId)
                .toList();

        log.info("Получено {} подписок для пользователя с id = {}", followingIds.size(), userId);

        return userFeignClient
                .findAllBy(followingIds, 0, page.getPageSize())
                .stream()
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UserShortDto> findFollowers(long userId, Pageable page) {
        log.info("Получение подписчиков текущего пользователя с id = {}", userId);

        Page<Subscription> followers = subscriptionRepository.findByFollowingId(userId, page);

        List<Long> followersIds = followers.get()
                .map(Subscription::getFollowerId)
                .toList();

        log.info("Получено {} подписчиков для пользователя с id = {}", followersIds.size(), userId);

        return userFeignClient
                .findAllBy(followersIds, 0, page.getPageSize())
                .stream()
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public SubscriptionDto follow(long userId, long followingId) {
        log.info("Попытка пользователя с id = {} подписаться на пользователя с id = {}", userId, followingId);

        if (userId == followingId) {
            throw new ConflictException("Пользователь с id = " + " не может подписаться сам на себя");
        }

        Subscription subscription = subscriptionRepository.save(
                subscriptionMapper.toSubscription(new Subscription(), userId, followingId)
        );
        log.info("Пользователь с id = {} успешно подписался на пользователя с id = {}", userId, followingId);
        SubscriptionDto subscriptionDto = subscriptionMapper.toSubscriptionShortDto(subscription);

        UserDto follower;
        try {
            follower = userFeignClient.findAllBy(List.of(userId), 0, 10).getFirst();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        UserDto following;
        try {
            following = userFeignClient.findAllBy(List.of(followingId), 0, 10).getFirst();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Пользователь с id = " + followingId + " не найден");
        }

        subscriptionDto.setFollower(
                userMapper.toUserShortDto(follower));
        subscriptionDto.setFollowing(
                userMapper.toUserShortDto(following));
        return subscriptionDto;
    }

    @Override
    @Transactional
    public void unfollow(long userId, long followingId) {
        log.info("Попытка пользователя с id = {} отписаться от пользователя с id = {}", userId, followingId);
        int deleteCount = subscriptionRepository.deleteByFollowingId(followingId);
        if (deleteCount == 0) {
            throw new ValidationException("Некорректно заданные параметры");
        }
        log.info("Пользователь с id = {} успешно отписался от пользователя с id = {}", userId, followingId);
    }
}
