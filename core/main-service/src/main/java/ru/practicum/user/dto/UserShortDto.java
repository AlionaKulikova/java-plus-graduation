package ru.practicum.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    @NotNull
    private Long id;
    @NotEmpty
    private String name;
}