package ewm.subscriptions.dto;

import ewm.user.dto.UserShortDto;
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
