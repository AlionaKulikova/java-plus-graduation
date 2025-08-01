package ru.practicum.ewm.service.action;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.model.action.Action;
import ru.practicum.ewm.model.action.ActionType;
import ru.practicum.ewm.repository.ActionRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final ActionRepository actionRepository;
    private final UserActionWeightConfig userActionWeightConfig;

    @Transactional
    public void addUserAction(Long userId, Long eventId, ActionType actionType, Instant timestamp) {
        Optional<Action> oldActionOpt = actionRepository.findByUserIdAndEventId(userId, eventId);

        if (oldActionOpt.isEmpty()) {
            Action action = new Action();
            action.setEventId(eventId);
            action.setUserId(userId);
            action.setActionType(actionType);
            action.setTimestamp(timestamp);
            actionRepository.save(action);
        } else {
            Action oldAction = oldActionOpt.get();
            double oldWeight = getUserActionWeight(oldAction.getActionType());
            double newWeight = getUserActionWeight(actionType);

            if (newWeight >= oldWeight && oldAction.getTimestamp().isBefore(timestamp)) {
                oldAction.setActionType(actionType);
                oldAction.setTimestamp(timestamp);
                actionRepository.save(oldAction);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Action> findActionsByEventIds(Set<Long> eventIds) {
        return actionRepository.findAllByEventIdIn(eventIds);
    }

    @Transactional(readOnly = true)
    public Set<Long> findEventIds(Long userId, Set<Long> eventIds) {
        return new HashSet<>(actionRepository.findDistinctEventIdByUserIdAndEventIdIn(userId, eventIds));
    }

    @Transactional(readOnly = true)
    public Set<Long> findSortedEventIdsOfUser(Long userId, int maxResult) {
        return new HashSet<>(actionRepository.findDistinctEventIdByUserIdOrderByTimestampDesc(
                userId,
                PageRequest.of(0, maxResult, Sort.by("timestamp").descending())
        ));
    }

    private double getUserActionWeight(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> userActionWeightConfig.getVIEW();
            case REGISTER -> userActionWeightConfig.getREGISTER();
            case LIKE -> userActionWeightConfig.getLIKE();
        };
    }
}