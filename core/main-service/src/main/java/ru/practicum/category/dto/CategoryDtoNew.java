package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDtoNew {
    @NotBlank(message = "Название категории обязательно.")
    @Size(min = 1, max = 50, message = "Ограничение длины названия категории. Не более 50 символов и не менее 1 символа.")
    private String name;
}