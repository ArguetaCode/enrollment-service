package com.campusenroll.enrollment_service.dto;

import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {

    private Long id;

    private Long studentId;

    private Long sectionId;

    private EnrollmentStatus status;

    private String paymentReference;

    private LocalDateTime createdAt;
}