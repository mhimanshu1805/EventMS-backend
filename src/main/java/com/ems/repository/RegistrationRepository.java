package com.ems.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.entity.Event;
import com.ems.entity.Registration;
import com.ems.entity.RegistrationStatus;
import com.ems.entity.User;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByEventAndParticipant(Event event, User participant);

    List<Registration> findByEventOrderByRegisteredAtDesc(Event event);

    List<Registration> findByParticipantOrderByRegisteredAtDesc(User participant);

    long countByEventAndStatusNot(Event event, RegistrationStatus status);

    long countByEventAndStatus(Event event, RegistrationStatus status);

    void deleteByEvent(Event event);
}
