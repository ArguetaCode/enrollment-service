package com.campusenroll.enrollment_service.integration.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long enrollmentId,
        Long studentId,
        BigDecimal amount,
        String status,
        String failureReason,
        LocalDateTime createdAt
) {
}
