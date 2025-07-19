package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.event.model.Location;

import jakarta.validation.constraints.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000, message = "The annotation field must be between 20 and 2000 characters long.")
    private String annotation;
    @NotNull
    @Positive
    private Long category;
    @NotBlank
    @Size(min = 20, max = 7000, message = "The description field must be between 20 and 7000 characters long.")
    private String description;
    @NotNull
    private String eventDate;
    @NotNull
    private Location location;
    private Boolean paid = false;
    @PositiveOrZero
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;
    @NotNull
    @Size(min = 3, max = 120, message = "The title field must be between 3 and 120 characters long.")
    private String title;
}
