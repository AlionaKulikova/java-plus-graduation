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
            log.error("URI or IP is null");
            throw new IllegalArgumentException("URI and IP cannot be null");
        }

        log.info("Creating stats for URI: {}, IP: {}", uri, ip);

        EndpointHit stats = EndpointHit.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();

        try {
            Object result = mainStatsClient.createStats(stats);
            log.info("Stats created successfully. Result: {}", result);
        } catch (Exception e) {
            log.error("Error creating stats for URI: {} and IP: {}", uri, ip, e);
            throw new RuntimeException("Failed to create stats", e);
        }
    }

    @Override
    public List<ViewStats> getStats(List<Long> eventsId, boolean unique) {
        if (eventsId == null || eventsId.isEmpty()) {
            log.info("Events list is null or empty");
            return Collections.emptyList();
        }

        log.info("Getting stats for events: {}, unique: {}", eventsId, unique);

        try {
            String start = LocalDateTime.now().minusYears(20).format(formatter);
            String end = LocalDateTime.now().plusYears(20).format(formatter);

            String[] uris = eventsId.stream()
                    .filter(id -> id != null)
                    .map(id -> String.format("/events/%d", id))
                    .toArray(String[]::new);

            if (uris.length == 0) {
                log.warn("No valid event IDs found");
                return Collections.emptyList();
            }

            List<ViewStats> stats = mainStatsClient.getStats(start, end, uris, unique);
            log.info("Retrieved {} stats entries", stats.size());
            return stats;
        } catch (Exception e) {
            log.error("Error getting stats for events: {}", eventsId, e);
            throw new RuntimeException("Failed to get stats", e);
        }
    }

    @Override
    public Map<Long, Long> getView(List<Long> eventsId, boolean unique) {
        if (eventsId == null || eventsId.isEmpty()) {
            log.info("Events list is null or empty");
            return Collections.emptyMap();
        }

        log.info("Getting views for events: {}, unique: {}", eventsId, unique);

        Map<Long, Long> views = new HashMap<>();

        try {
            List<ViewStats> stats = getStats(eventsId, unique);

            // Initialize views with 0 for all event IDs
            eventsId.forEach(id -> views.put(id, 0L));

            for (ViewStats stat : stats) {
                String uriPath = stat.getUri();
                if (uriPath != null && uriPath.startsWith("/events/")) {
                    try {
                        Long id = Long.valueOf(uriPath.substring("/events/".length()));
                        if (eventsId.contains(id)) {  // Only count views for requested events
                            Long hits = stat.getHits() != null ? stat.getHits() : 0L;
                            views.put(id, hits);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid event ID in URI: {}", uriPath);
                    }
                }
            }

            log.info("Processed views: {}", views);
            return views;
        } catch (Exception e) {
            log.error("Error processing views for events: {}", eventsId, e);
            throw new RuntimeException("Failed to process views", e);
        }
    }
}
