package ru.practicum.stat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.stat.client.MainStatsClient;
import ru.practicum.stat.service.MainStatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainStatsServiceImpl implements MainStatsService {
    private static final String APP_NAME = "ewm-main-service";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private final MainStatsClient mainStatsClient;

    @Override
    public void createStats(String uri, String ip) {
        if (uri == null || ip == null) {
            log.error("URI или IP не указаны");
            throw new IllegalArgumentException("URI и IP не могут быть null");
        }
        log.info("Создание статистики для URI: {}, IP: {}", uri, ip);
        EndpointHit stats = EndpointHit.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();

        try {
            Object result = mainStatsClient.createStats(stats);
            log.info("Статистика успешно создана. Результат: {}", result);
        } catch (Exception e) {
            log.error("Ошибка при создании статистики для URI: {} и IP: {}", uri, ip, e);
            throw new RuntimeException("Не удалось создать статистику", e);
        }
    }

    @Override
    public List<ViewStats> getStats(List<Long> eventsId, boolean unique) {
        if (eventsId == null || eventsId.isEmpty()) {
            log.info("Список событий пуст или не указан");
            return Collections.emptyList();
        }

        log.info("Получение статистики для событий: {}, уникальные: {}", eventsId, unique);
        try {
            String start = LocalDateTime.now().minusYears(20).format(formatter);
            String end = LocalDateTime.now().plusYears(20).format(formatter);

            String[] uris = eventsId.stream()
                    .filter(id -> id != null)
                    .map(id -> String.format("/events/%d", id))
                    .toArray(String[]::new);

            if (uris.length == 0) {
                log.warn("Не найдено корректных ID событий");
                return Collections.emptyList();
            }
            List<ViewStats> stats = mainStatsClient.getStats(start, end, uris, unique);
            log.info("Получено {} записей статистики", stats.size());

            return stats;
        } catch (Exception e) {
            log.error("Ошибка при получении статистики для событий: {}", eventsId, e);
            throw new RuntimeException("Не удалось получить статистику", e);
        }
    }

    @Override
    public Map<Long, Long> getView(List<Long> eventsId, boolean unique) {
        if (eventsId == null || eventsId.isEmpty()) {
            log.info("Список событий пуст или не указан");
            return Collections.emptyMap();
        }

        log.info("Получение просмотров для событий: {}, уникальные: {}", eventsId, unique);
        Map<Long, Long> views = new HashMap<>();

        try {
            List<ViewStats> stats = getStats(eventsId, unique);

            // Инициализация просмотров нулями для всех ID событий
            eventsId.forEach(id -> views.put(id, 0L));

            for (ViewStats stat : stats) {
                String uriPath = stat.getUri();
                if (uriPath != null && uriPath.startsWith("/events/")) {
                    try {
                        Long id = Long.valueOf(uriPath.substring("/events/".length()));
                        if (eventsId.contains(id)) {  // Учитываем только запрошенные события
                            Long hits = stat.getHits() != null ? stat.getHits() : 0L;
                            views.put(id, hits);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Некорректный ID события в URI: {}", uriPath);
                    }
                }
            }

            log.info("Обработанные просмотры: {}", views);
            return views;
        } catch (Exception e) {
            log.error("Ошибка при обработке просмотров для событий: {}", eventsId, e);
            throw new RuntimeException("Не удалось обработать просмотры", e);
        }
    }
}

