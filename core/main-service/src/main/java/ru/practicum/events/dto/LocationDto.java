package ru.practicum.events.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.events.model.Location;

/**
 * DTO for {@link Location}
 */
@Data
public class LocationDto {
    @NotNull
    Float lat;
    @NotNull
    Float lon;
}