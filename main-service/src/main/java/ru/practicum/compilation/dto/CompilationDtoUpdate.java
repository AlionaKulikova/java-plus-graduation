package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDtoUpdate {
    @Builder.Default
    private List<Long> events = new ArrayList<>();
    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Длина заголовка от 1 до 50 символов")
    private String title;
}
