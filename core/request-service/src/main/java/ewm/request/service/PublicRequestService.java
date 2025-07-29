package ewm.request.service;


import ewm.interaction.dto.request.ParticipationRequestDto;

import java.util.List;

public interface PublicRequestService {
    List<ParticipationRequestDto> getSentBy(long userId);

    ParticipationRequestDto send(Long userId, Long eventId);

    ParticipationRequestDto cancel(long requestId, long userId);
}
