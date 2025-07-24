package ewm.interaction.dto.event.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import ewm.interaction.dto.event.validation.EventDateInTwoHours;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank
    @Length(min = 20, max = 2000)
    String annotation;
    Long category;
    @NotBlank
    @Length(min = 20, max = 7000)
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @EventDateInTwoHours
    LocalDateTime eventDate;
    Location location;
    boolean paid;
    @PositiveOrZero
    Integer participantLimit;
    boolean requestModeration = true;
    @Length(min = 3, max = 120)
    String title;
}