package com.minden.service;

import java.util.List;
import java.util.stream.Collectors;

import com.minden.dto.EventDto;
import com.minden.repository.EventRepository;

public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<EventDto> findAll() {
        return eventRepository.findAll().stream()
                .map(event -> new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getMinGoldPenalty(),
                event.getMaxGoldPenalty()))
                .collect(Collectors.toList());
    }

}
