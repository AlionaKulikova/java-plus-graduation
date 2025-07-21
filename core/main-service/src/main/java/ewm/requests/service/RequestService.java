package ewm.requests.service;

import ewm.requests.dto.ParticipationRequestDto;
import ewm.requests.dto.RequestUpdateDto;
import ewm.requests.dto.RequestUpdateResultDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    RequestUpdateResultDto updateEventRequests(Long userId, Long eventId, RequestUpdateDto updateDto);
}
