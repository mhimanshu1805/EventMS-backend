package com.ems.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.entity.Event;
import com.ems.entity.EventStatus;
import com.ems.entity.User;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Event> findByOrganizerOrderByCreatedAtDesc(User organizer);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByStatusAndTitleContainingIgnoreCase(EventStatus status, String title);
}
