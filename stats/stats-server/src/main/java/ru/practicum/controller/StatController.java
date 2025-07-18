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
        log.info("Эндпоинт /stats. GET запрос. Получение статистики по посещениям.");
        List<ViewStats> stats = hitService.getStats(start, end, uris, unique);

        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    @PostMapping("/hit")
    @Transactional
    public ResponseEntity<EndpointHit> addHit(@RequestBody EndpointHit endpointHit) {
        log.info("Эндпоинт /hit. POST запрос. Создание объекта статистики {}.", endpointHit);
        EndpointHit status = hitService.addHit(endpointHit);

        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }
}