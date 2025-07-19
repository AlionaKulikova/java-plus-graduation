package ru.practicum.compilation.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private Boolean pinned = false;
    @NotBlank
    @Size(min = 1, max = 50, message = "Длина должна быть от 1 до 50 символов")
    private String title;
    private List<Long> events;
}
