package ru.practicum.ewm.service.similarity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.configuration.UserActionWeightConfig;
import ru.practicum.ewm.model.similarity.Similarity;
import ru.practicum.ewm.repository.SimilarityRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SimilarityService {
    private final SimilarityRepository repository;
    private final UserActionWeightConfig userActionWeightConfig;

    @Transactional(readOnly = true)
    public List<Similarity> findAllContainsEventId(Long eventId) {
        return repository.findAllContainsEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<Similarity> findNPairContainsEventIdsSortedDescScore(Set<Long> eventIds, int maxResults) {
        Pageable pageable = PageRequest.of(0, maxResults);

        return repository.findNPairContainsEventIdsSortedDescScore(eventIds, pageable);
    }
}