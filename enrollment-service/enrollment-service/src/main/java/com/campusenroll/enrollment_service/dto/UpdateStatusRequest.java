package com.campusenroll.enrollment_service.dto;

import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    private EnrollmentStatus status;
}
