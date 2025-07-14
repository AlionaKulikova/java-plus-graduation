package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDtoRequest {
    @Builder.Default
    private List<Long> events = new ArrayList<>();
    private Boolean pinned;

    @NotBlank(message = "Заголовок должен быть заполнен")
    @Size(min = 1, max = 50, message = "Длина заголовка от 1 до 50 символов")
    private String title;
}