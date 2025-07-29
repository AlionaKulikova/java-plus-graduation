package ewm.interaction.dto.subscription;

import ewm.interaction.dto.user.UserShortDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionDto {
    Long id;
    UserShortDto follower;
    UserShortDto following;
    LocalDateTime created;
}
