package com.ems.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dto.AdminUserResponse;
import com.ems.dto.EventResponse;
import com.ems.entity.User;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.UserRepository;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final EventService eventService;

    public AdminService(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    public AdminUserResponse setActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!active && user.getEmail().equalsIgnoreCase(currentEmail())) {
            throw new BadRequestException("Admin cannot deactivate their own account");
        }
        user.setActive(active);
        return toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listEvents() {
        return eventService.listAll();
    }

    public void deleteEvent(Long eventId) {
        eventService.deleteAny(eventId);
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private AdminUserResponse toUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }
}
