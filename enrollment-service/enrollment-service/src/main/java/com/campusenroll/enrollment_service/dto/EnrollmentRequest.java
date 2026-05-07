package com.campusenroll.enrollment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
}
