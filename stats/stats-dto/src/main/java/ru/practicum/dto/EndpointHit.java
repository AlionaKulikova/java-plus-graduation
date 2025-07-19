package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHit {
    private Long id;
    @NotBlank(message = "Название приложения не может быть пустым.")
    private String app;
    @NotBlank(message = "URI не может быть пустым.")
    private String uri;
    @NotBlank(message = "IP-адрес не может быть пустым.")
    private String ip;
    @NotBlank(message = "Временная метка не может быть пустой.")
    private String timestamp;
}