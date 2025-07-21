package ewm.requests.mapper;

import org.springframework.stereotype.Component;
import ewm.events.model.Event;
import ewm.requests.dto.ParticipationRequestDto;
import ewm.requests.model.Request;
import ewm.requests.model.RequestStatus;
import ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RequestMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParticipationRequestDto toDto(Request request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated().format(FORMATTER));
        dto.setEvent(request.getEvent().getId());
        dto.setRequester(request.getRequester().getId());
        dto.setStatus(request.getStatus().name());
        return dto;
    }

    public Request toEntity(ParticipationRequestDto dto, Event event, User requester) {
        Request request = new Request();
        request.setId(dto.getId());
        request.setCreated(dto.getCreated() != null ? LocalDateTime.parse(dto.getCreated(), FORMATTER) : null);
        request.setEvent(event);
        request.setRequester(requester);
        request.setStatus(RequestStatus.valueOf(dto.getStatus()));
        return request;
    }
}