package ewm.interaction.dto.event.event;

import ewm.interaction.dto.event.validation.StartAndEndValid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@StartAndEndValid
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParamsEventPublic {
    String text;

    List<Long> categories;

    Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime rangeEnd;

    boolean onlyAvailable;

    String sort;
}