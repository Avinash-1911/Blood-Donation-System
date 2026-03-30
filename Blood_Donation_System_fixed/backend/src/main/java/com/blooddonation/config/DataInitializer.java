package com.blooddonation.config;

import com.blooddonation.model.User;
import com.blooddonation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if no admins exist
        if (!userRepository.existsByEmail("admin@blooddonation.com")) {
            User admin = User.builder()
                    .name("System Administrator")
                    .email("admin@blooddonation.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .phone("+911234567890")
                    .roles(List.of(User.Role.ROLE_ADMIN))
                    .active(true)
                    .build();
            userRepository.save(admin);
            logger.info("==============================================");
            logger.info("Default admin created:");
            logger.info("  Email   : admin@blooddonation.com");
            logger.info("  Password: Admin@123");
            logger.info("  ⚠️  Please change the password after first login!");
            logger.info("==============================================");
        }
    }
}
