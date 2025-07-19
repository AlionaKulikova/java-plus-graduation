package ru.practicum.event.repository;

import ru.practicum.event.dto.EventAdminParams;
import ru.practicum.event.dto.EventPublicParams;
import ru.practicum.event.model.Event;

import java.util.List;

public interface CustomizedEventStorage {
    List<Event> searchEventsForAdmin(EventAdminParams param);

    List<Event> searchPublicEvents(EventPublicParams param);
}