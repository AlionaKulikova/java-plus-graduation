package ewm.request.controller;

import ewm.interaction.dto.request.ParticipationRequestDto;
import ewm.interaction.util.PathConstants;
import ewm.request.service.interfaces.RequestServicePrivate;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(PathConstants.PRIVATE_REQUESTS)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestControllerPrivate {
    private final RequestServicePrivate requestService;

    @GetMapping()
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @NotNull @RequestParam Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping(PathConstants.PRIVATE_REQUEST_CANCEL)
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequest(requestId, userId);
    }
}