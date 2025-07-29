package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.model.Action;
import ru.practicum.ewm.model.ActionType;
import ru.practicum.ewm.repository.ActionRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final ActionRepository repository;
    private final UserActionWeightConfig userActionWeightConfig;

    // сохранение истории действия пользователя в БД если изменился вес действия в большую сторону
    @Transactional
    public void addAction(long userId, long eventId, ActionType actionType, Instant timestamp) {
        Optional<Action> oldActionOpt = repository.findByUserIdAndEventId(userId, eventId);
        if (oldActionOpt.isEmpty()) {
            Action action = new Action();
            action.setEventId(eventId);
            action.setUserId(userId);
            action.setActionType(actionType);
            action.setTimestamp(timestamp);
            repository.save(action);
        } else {
            Action oldAction = oldActionOpt.get();
            double oldWeight = getUserActionWeight(oldAction.getActionType());
            double newWeight = getUserActionWeight(actionType);
            if (newWeight >= oldWeight) {
                oldAction.setActionType(actionType);
                oldAction.setTimestamp(timestamp);
                Instant oldTimestamp = oldAction.getTimestamp();
                if (oldTimestamp.isBefore(timestamp)) {
                    oldAction.setTimestamp(timestamp);
                }
                repository.save(oldAction);
            }
        }
    }

    @Transactional(readOnly = true)
    public Set<Long> findAllByUserIdAndEventIdIn(long userId, Set<Long> eventIds) {
        return new HashSet<>(repository.findEventIdsByUserIdAndEventIdIn(userId, eventIds));
    }

    @Transactional(readOnly = true)
    public Set<Long> findByUserIdOrderByTimestampDesc(long userId, int maxResult) {
        return new HashSet<>(repository.findByUserIdOrderByTimestampDesc(userId, maxResult));
    }

    @Transactional(readOnly = true)
    public List<Action> findAllByEventIds(Set<Long> eventIds) {
        return repository.findAllByEventIdIn(eventIds).stream().toList();
    }

    private double getUserActionWeight(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> userActionWeightConfig.getVIEW();
            case REGISTER -> userActionWeightConfig.getREGISTER();
            case LIKE -> userActionWeightConfig.getLIKE();
        };
    }
}
