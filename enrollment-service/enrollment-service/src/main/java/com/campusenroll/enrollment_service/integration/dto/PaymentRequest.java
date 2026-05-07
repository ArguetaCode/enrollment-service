package com.campusenroll.enrollment_service.integration.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        Long enrollmentId,
        Long studentId,
        BigDecimal amount,
        boolean simulateFailure
) {
}
