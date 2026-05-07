package com.campusenroll.enrollment_service.controller;

import com.campusenroll.enrollment_service.dto.EnrollmentRequest;
import com.campusenroll.enrollment_service.dto.EnrollmentResponse;
import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService service;

    @PostMapping("/enrollments")
    public EnrollmentResponse createEnrollment(
            @Valid @RequestBody
            EnrollmentRequest request
    ) {

        return service.createEnrollment(request);
    }

    @GetMapping("/enrollments/{id}")
    public EnrollmentResponse getEnrollmentById(
            @PathVariable Long id
    ) {

        return service.getEnrollmentById(id);
    }

    @GetMapping("/students/{studentId}/enrollments")
    public List<Enrollment> getStudentEnrollments(
            @PathVariable Long studentId
    ) {

        return service.getStudentEnrollments(studentId);
    }

    @DeleteMapping("/enrollments/{id}")
    public void deleteEnrollment(
            @PathVariable Long id
    ) {

        service.deleteEnrollment(id);
    }
}