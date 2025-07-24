package ewm.interaction.dto.subscription;

import ewm.interaction.dto.user.UserShortDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionDto {
    private Long id;
    private UserShortDto subscriber;
    private UserShortDto subscribed;
    private String created;
}
