package com.minden.repository;

import java.util.List;
import java.util.Optional;

import com.minden.entity.Event;

public interface EventRepository {
    Optional<Event> findById(Integer id);

    List<Event> findAll();
}
