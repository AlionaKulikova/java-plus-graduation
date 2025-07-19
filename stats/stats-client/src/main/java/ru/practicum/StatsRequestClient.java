package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatsRequestClient extends BaseRequestClient {

    @Autowired
    public StatsRequestClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl)).build());
    }

    public ResponseEntity<Object> postHit(EndpointHit endpointHit) {
        log.info("Выполнение запрос на добавление события вызова сервиса в статистику через модуль client");
        return post(endpointHit);
    }

    public ResponseEntity<Object> getViewStats(String start, String end, List<String> uris, boolean unique) {
        StringBuilder uri = new StringBuilder();
        for (String u : uris) {
            uri.append("&uris=").append(u);
        }
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "unique", unique);
        log.info("Выполнение запрос на получение статистики через модуль client");
        return get("/stats?start={start}&end={end}" + uri + "&unique={unique}", parameters);
    }
}