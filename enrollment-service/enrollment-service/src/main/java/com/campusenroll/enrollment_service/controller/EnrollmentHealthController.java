package com.campusenroll.enrollment_service.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnrollmentHealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "enrollment-service",
                "status", "UP"
        );
    }
}
