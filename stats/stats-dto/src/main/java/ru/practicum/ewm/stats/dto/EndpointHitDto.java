package ru.practicum.ewm.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitDto {
    @NotBlank(message = "Поле app не может быть null")
    String app;

    @NotBlank(message = "Поле uri не может быть null")
    String uri;

    @NotBlank(message = "Поле ip не может быть null")
    String ip;

    @NotNull(message = "Поле timestamp не может быть null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
}
