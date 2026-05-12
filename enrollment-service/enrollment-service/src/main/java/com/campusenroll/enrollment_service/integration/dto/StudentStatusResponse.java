package com.campusenroll.enrollment_service.integration.dto;

public record StudentStatusResponse(
        Long studentId,
        String status,
        boolean active
) {
}
