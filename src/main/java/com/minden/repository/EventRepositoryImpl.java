package com.minden.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.minden.entity.ConnectionPool;
import com.minden.entity.Event;

public class EventRepositoryImpl implements EventRepository {
    private final ConnectionPool connectionPool;

    public EventRepositoryImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Optional<Event> findById(Integer id) {
        String sql = "SELECT * FROM event WHERE id = ?";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                Event event = Event.builder()
                    .id(rs.getObject("id", Integer.class))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .minGoldPenalty(rs.getObject("min_gold_penalty", Integer.class))
                    .maxGoldPenalty(rs.getObject("max_gold_penalty", Integer.class))
                    .build();
                return Optional.of(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    @Override
    public List<Event> findAll() {
        String sql = "SELECT * FROM event";
        List<Event> events = new ArrayList<>();
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                Event event = Event.builder()
                    .id(rs.getObject("id", Integer.class))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .minGoldPenalty(rs.getObject("min_gold_penalty", Integer.class))
                    .maxGoldPenalty(rs.getObject("max_gold_penalty", Integer.class))
                    .build();
                    events.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return events;
    }
}
