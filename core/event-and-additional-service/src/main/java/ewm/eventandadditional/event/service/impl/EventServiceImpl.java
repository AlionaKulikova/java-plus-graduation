package ewm.eventandadditional.event.service.impl;

import ewm.eventandadditional.category.mapper.UserMapper;
import ewm.eventandadditional.event.mappers.EventMapper;
import ewm.eventandadditional.event.model.Event;
import ewm.eventandadditional.event.repository.EventRepository;
import ewm.eventandadditional.event.service.EventService;
import ewm.interaction.dto.eventandadditional.event.EventFullDto;
import ewm.interaction.exception.NotFoundException;
import ewm.interaction.feign.UserFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceImpl implements EventService {
    final EventRepository eventRepository;
    final EventMapper eventMapper;
    final UserMapper userMapper;
    final UserFeignClient userFeignClient;

    @Override
    public EventFullDto getBy(Long eventId, Long userId) {
        EventFullDto eventFullDto = eventMapper
                .toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId));
        eventFullDto.setInitiator(userMapper.toUserShortDto(
                userFeignClient.findAllBy(List.of(userId), 0, 10).getFirst()
        ));
        return eventFullDto;
    }

    @Override
    public EventFullDto getBy(long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            throw new NotFoundException("Событие с id = " + eventId + " не найдено");
        }
        EventFullDto eventFullDto = eventMapper
                .toEventFullDto(optionalEvent.get());
        eventFullDto.setInitiator(userMapper.toUserShortDto(
                userFeignClient.findAllBy(List.of(optionalEvent.get().getInitiatorId()),
                        0, 10).getFirst()));
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getBy(Set<Long> eventIds) {
        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        return events.stream().map(eventMapper::toEventFullDto).toList();
    }
}
