package ewm.interaction.dto.event.compilation;

import ewm.interaction.dto.event.event.EventShortDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    Long id;

    boolean pinned;

    String title;

    Set<EventShortDto> events;
}