package com.ems.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.repository.UserRepository;

/**
 * Creates one MAIN_ADMIN on first startup. Registration never allows this role.
 */
@Component
public class AdminDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminDataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ems.admin.email}") String adminEmail,
            @Value("${ems.admin.password}") String adminPassword,
            @Value("${ems.admin.name}") String adminName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.MAIN_ADMIN)) {
            return;
        }
        User admin = User.builder()
                .name(adminName)
                .email(adminEmail.trim().toLowerCase())
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.MAIN_ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
        log.info("Seeded MAIN_ADMIN user with email {}", admin.getEmail());
    }
}
