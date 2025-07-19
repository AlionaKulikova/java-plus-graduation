package ewm.events.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ewm.events.model.Location;

/**
 * DTO for {@link Location}
 */
@Data
public class LocationDto {
    @NotNull
    Float lat;
    @NotNull
    Float lon;
}