package com.campusenroll.enrollment_service.integration.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentEvent(
        String eventId,
        String eventType,
        Long paymentId,
        Long enrollmentId,
        Long studentId,
        BigDecimal amount,
        String status,
        LocalDateTime occurredAt
) {
}
