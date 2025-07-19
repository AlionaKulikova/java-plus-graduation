package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHit {
    private Long id;

    @NotBlank(message = "App name shouldn't be blank.")
    @Pattern(regexp = "^ewm-main-service$", message = "App name must be 'ewm-main-service'.")
    private String app;

    @NotBlank(message = "URI shouldn't be blank.")
    @Pattern(regexp = "^/events/\\d+$", message = "URI must match the format '/events/{id}'")
    private String uri;

    @NotBlank(message = "IP shouldn't be blank.")
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "Invalid IP address format.")
    private String ip;

    @NotBlank(message = "Timestamp shouldn't be blank.")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z?",
            message = "Timestamp must be in ISO format 'yyyy-MM-dd HH:mm:ss[.SSS]Z'")
    private String timestamp;
}
