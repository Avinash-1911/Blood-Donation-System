package com.blooddonation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AppConfig {
    // Async executor enabled via @EnableAsync
    // MongoDB auditing enabled in BloodDonationApplication via @EnableMongoAuditing
}
