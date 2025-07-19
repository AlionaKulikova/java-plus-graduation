package ru.practicum.event.dto;

import lombok.*;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventAdminParams {

    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
    private Pageable pageable;
}
