package ru.practicum.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.exceptions.DataTimeException;
import ru.practicum.mappper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.model.StatsMapper;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;
    private final HitMapper hitMapper;
    private final StatsMapper statsMapper;

    @Transactional
    public EndpointHit addHit(EndpointHit endpointHit) {
        Hit hit = hitMapper.toHit(endpointHit);
        log.info("Сохранение в базу информации о запросе {}", hit);
        hit.setTimestamp(LocalDateTime.now());
        Hit newHit = hitRepository.save(hit);
        return statsMapper.toCreationDto(newHit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        if (end.isBefore(start)) {
            throw new DataTimeException("Дата окончания не может быть раньше даты начала");
        }
        List<ViewStats> statistic;
        if (unique) { // Вывод статистики только для уникальных запросов
            if (uris == null || uris.isEmpty()) {
                log.info("Получение статистики уникальных запросов для серверов где URIs пустой");
                statistic = hitRepository.findAllUniqueHitsWhenUriIsEmpty(start, end);
            } else {
                log.info("Получение статистики уникальных запросов для перечисленных URIs");
                statistic = hitRepository.findAllUniqueHitsWhenUriIsNotEmpty(start, end, uris);
            }
        } else { // Вывод статистики для всех запросов
            if (uris == null || uris.isEmpty()) {
                log.info("Получение статистики без учета уникальных запросов для серверов где URIs пустой");
                statistic = hitRepository.findAllHitsWhenUriIsEmpty(start, end);
            } else {
                log.info("Получение статистики без учета уникальных запросов для перечисленных URIs");
                statistic = hitRepository.findAllHitsWhenStarEndUris(start, end, uris);
            }
        }
        return statistic;
    }
}
