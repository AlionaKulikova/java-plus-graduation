package ru.practicum.service;

import ru.practicum.StatDto;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    StatDto createHit(StatDto dto);

    List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}