package com.ems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.entity.Event;
import com.ems.entity.Query;
import com.ems.entity.User;

public interface QueryRepository extends JpaRepository<Query, Long> {

    List<Query> findByEventOrderByCreatedAtDesc(Event event);

    List<Query> findByParticipantOrderByCreatedAtDesc(User participant);

    void deleteByEvent(Event event);
}
