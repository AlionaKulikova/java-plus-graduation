package ewm.subscriptions.dto;

import lombok.Getter;
import lombok.Setter;
import ewm.user.dto.UserShortDto;

@Getter
@Setter
public class SubscriptionDto {
    private Long id;
    private UserShortDto subscriber;
    private UserShortDto subscribedTo;
    private String created;
}
