package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.model.Similarity;
import ru.practicum.ewm.repository.SimilarityRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SimilarityService {
    private final SimilarityRepository repository;
    private final UserActionWeightConfig userActionWeightConfig;

    @Transactional(readOnly = true)
    public List<Similarity> findAllContainsEventId(long eventId) {
        return repository.findAllContainsEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<Similarity> findNPairContainsEventIdsSortedDescScore(Set<Long> eventIds, int maxResults) {
        return repository.findNPairContainsEventIdsSortedDescScore(eventIds, maxResults);
    }
}
