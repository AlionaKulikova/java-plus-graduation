package ewm.subscriptions.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.subscriptions.dto.SubscriptionDto;
import ewm.subscriptions.mappers.SubscriptionMapper;
import ewm.subscriptions.model.QSubscription;
import ewm.subscriptions.model.Subscription;
import ewm.subscriptions.repository.SubscriptionRepository;
import ewm.user.dto.UserShortDto;
import ewm.user.mappers.UserMapper;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionServiceImpl implements SubscriptionService {
    final SubscriptionRepository subscriptionRepository;
    final UserRepository userRepository;
    final SubscriptionMapper subscriptionMapper;
    final UserMapper userMapper;
    final JPAQueryFactory jpaQueryFactory;
    final QSubscription qSubscription = QSubscription.subscription;

    @Override
    public Set<UserShortDto> findFollowing(long userId, Pageable page) {
        log.info("Получение подписок текущего пользователя с id = {}", userId);
        Set<UserShortDto> following = jpaQueryFactory
                .selectFrom(qSubscription)
                .leftJoin(qSubscription.following).fetchJoin()
                .leftJoin(qSubscription.follower).fetchJoin()
                .where(qSubscription.follower.id.eq(userId))
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .stream()
                .map(Subscription::getFollowing)
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet());
        log.info("Получено {} подписок для пользователя с id = {}", following.size(), userId);
        return following;
    }

    @Override
    public Set<UserShortDto> findFollowers(long userId, Pageable page) {
        log.info("Получение подписчиков текущего пользователя с id = {}", userId);
        Set<UserShortDto> followers = jpaQueryFactory
                .selectFrom(qSubscription)
                .leftJoin(qSubscription.following).fetchJoin()
                .leftJoin(qSubscription.follower).fetchJoin()
                .where(qSubscription.following.id.eq(userId))
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .stream()
                .map(Subscription::getFollower)
                .map(userMapper::toUserShortDto)
                .collect(Collectors.toSet());
        log.info("Получено {} подписчиков для пользователя с id = {}", followers.size(), userId);
        return followers;
    }

    @Override
    @Transactional
    public SubscriptionDto follow(long userId, long followingId) {
        log.info("Попытка пользователя с id = {} подписаться на пользователя с id = {}", userId, followingId);

        if (userId == followingId) {
            throw new ConflictException("Пользователь с id = " + " не может подписаться сам на себя");
        }

        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + followingId + " не найден"));
        Subscription subscription = subscriptionRepository.save(
                subscriptionMapper.toSubscription(new Subscription(), follower, following)
        );
        log.info("Пользователь с id = {} успешно подписался на пользователя с id = {}", userId, followingId);
        return subscriptionMapper.toSubscriptionShortDto(subscription);
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
