package ewm.compilation.dto;

import lombok.*;
import ewm.events.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDtoResponse {
    private Long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}
