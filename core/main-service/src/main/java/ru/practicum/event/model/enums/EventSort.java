package ru.practicum.event.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventSort {
    EVENT_DATE("EVENT_DATE"),
    VIEWS("VIEWS");

    private final String title;
}