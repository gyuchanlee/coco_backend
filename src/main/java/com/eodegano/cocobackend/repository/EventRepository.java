package com.eodegano.cocobackend.repository;

import com.eodegano.cocobackend.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByContentidIn(List<Long> contentIds);
}
