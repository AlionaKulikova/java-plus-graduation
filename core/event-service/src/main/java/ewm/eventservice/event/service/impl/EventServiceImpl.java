package ewm.eventservice.event.service.impl;

import ewm.eventservice.category.mapper.UserMapper;
import ewm.eventservice.event.mappers.EventMapper;
import ewm.eventservice.event.model.Event;
import ewm.eventservice.event.repository.EventRepository;
import ewm.eventservice.event.service.interfaces.EventService;
import ewm.interaction.dto.event.event.EventFullDto;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.feign.UserFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserFeignClient userFeignClient;
    private final UserMapper userMapper;

    @Override
    public EventFullDto getEventOfUser(Long eventId, Long userId) {
        EventFullDto eventFullDto = eventMapper
                .toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId));
        eventFullDto.setInitiator(userMapper.toUserShortDto(
                userFeignClient.getUsers(List.of(userId), 0, 10).getFirst()
        ));

        return eventFullDto;
    }

    @Override
    public EventFullDto getEventByID(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);

        if (optionalEvent.isEmpty()) {
            throw new NotFoundException("Событие с ID=" + eventId + " не найдено");
        }
        EventFullDto eventFullDto = eventMapper
                .toEventFullDto(optionalEvent.get());
        eventFullDto.setInitiator(userMapper.toUserShortDto(
                userFeignClient.getUsers(List.of(optionalEvent.get().getInitiatorId()),
                        0, 10).getFirst()));

        return eventFullDto;
    }
}