package ru.practicum.subscriptions.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.subscriptions.dto.SubscriptionDto;
import ru.practicum.subscriptions.mapper.SubscriptionMapper;
import ru.practicum.subscriptions.model.Subscription;
import ru.practicum.subscriptions.repository.SubscriptionRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public SubscriptionDto subscribe(Long subscriberId, Long subscribedToId) {
        if (subscriberId.equals(subscribedToId)) {
            throw new ConflictException("Пользователь не может подписаться сам на себя");
        }
        User subscriber = checkUserExists(subscriberId);
        User subscribedTo = checkUserExists(subscribedToId);
        if (subscriptionRepository.existsBySubscriberIdAndSubscribedToId(subscriberId, subscribedToId)) {
            throw new ConflictException("Подписка уже оформлена");
        }
        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscriber);
        subscription.setSubscribedTo(subscribedTo);
        subscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(subscription);
    }

    @Override
    public void unsubscribe(Long subscriberId, Long subscribedToId) {
        int deletedCount = subscriptionRepository.deleteBySubscriberIdAndSubscribedToId(subscriberId, subscribedToId);
        if (deletedCount == 0) {
            throw new NotFoundException("Подписка не найдена");
        }
    }

    @Override
    public List<SubscriptionDto> getSubscriptions(Long userId) {
        return subscriptionRepository.findBySubscriberId(userId).stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Override
    public List<SubscriptionDto> getSubscribers(Long userId) {
        return subscriptionRepository.findBySubscribedToId(userId).stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    private User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID=" + userId + " не найден"));
    }
}
