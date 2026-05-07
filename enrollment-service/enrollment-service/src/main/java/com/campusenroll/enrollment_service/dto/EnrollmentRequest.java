package com.campusenroll.enrollment_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollmentRequest {

    @NotNull(
            message = "El studentId es obligatorio"
    )
    private Long studentId;

    @NotNull(
            message = "El sectionId es obligatorio"
    )
    private Long sectionId;

    @NotNull(
            message = "El amount es obligatorio"
    )
    @DecimalMin(
            value = "0.01",
            message = "El amount debe ser mayor a 0"
    )
    private BigDecimal amount;

    private boolean simulatePaymentFailure;
}
