package ewm.request.service.interfaces;

import ewm.interaction.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestServicePrivate {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long requestId, Long userId);
}