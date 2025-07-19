package ru.practicum.compilation.dto;

import lombok.*;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    private Long id;
    private Boolean pinned;
    private String title;
    private List<EventShortDto> events;
}
