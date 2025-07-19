package ru.practicum.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatController {

    private final HitService hitService;

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
        log.info("Вызван метода с запросом статистики в период с {} до {}, для следующих серверов {}. " +
                "Выбраны только уникальные значения - {}", start, end, uris, unique);
        // Получение статистики
        List<ViewStats> stats = hitService.getStats(start, end, uris, unique);

        // Логирование возвращаемого значения
        log.info("Статистика получена: {}", stats);

        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    @PostMapping("/hit")
    @Transactional
    public ResponseEntity<EndpointHit> addHit(@RequestBody EndpointHit endpointHit) {
        log.info("Вызван метод добавления записи в статистику {}", endpointHit);
        // Логирование результата
        EndpointHit status = hitService.addHit(endpointHit);
        log.info("Результат добавления записи: {}", status);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }
}