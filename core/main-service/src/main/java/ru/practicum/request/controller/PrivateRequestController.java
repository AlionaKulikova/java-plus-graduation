package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@Validated
@RestController
@RequestMapping("users/{userId}/requests")
public interface PrivateRequestController {

    @GetMapping()
    List<ParticipationRequestDto> getRequest(@PathVariable Long userId);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto setRequest(
            @Positive @RequestParam Long eventId,
            @Positive @PathVariable Long userId);

    @PatchMapping("/{requestId}/cancel")
    ParticipationRequestDto updateRequest(@PathVariable Long userId, @PathVariable Long requestId);

}
