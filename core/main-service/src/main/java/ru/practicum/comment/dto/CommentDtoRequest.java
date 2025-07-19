package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommentDtoRequest {
    @NotBlank(message = "Сообщение не может быть пустым.")
    @Size(min = 2, max = 1000, message = "Размер сообщения от 2 до 1000 символов.")
    private String message;
}
