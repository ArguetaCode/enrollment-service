package com.campusenroll.enrollment_service.service;

import com.campusenroll.enrollment_service.dto.EnrollmentRequest;
import com.campusenroll.enrollment_service.dto.EnrollmentResponse;
import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository repository;

    @Transactional
    public EnrollmentResponse createEnrollment(
            EnrollmentRequest request
    ) {

        boolean exists =
                repository.existsByStudentIdAndSectionId(
                        request.getStudentId(),
                        request.getSectionId()
                );

        if (exists) {

            throw new RuntimeException(
                    "El estudiante ya está inscrito"
            );
        }

        Enrollment enrollment =
                Enrollment.builder()
                        .studentId(request.getStudentId())
                        .sectionId(request.getSectionId())
                        .build();

        Enrollment saved =
                repository.save(enrollment);

        return mapToResponse(saved);
    }

    public EnrollmentResponse getEnrollmentById(
            Long id
    ) {

        Enrollment enrollment =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inscripción no encontrada"
                                )
                        );

        return mapToResponse(enrollment);
    }

    public List<Enrollment> getStudentEnrollments(
            Long studentId
    ) {

        return repository.findByStudentId(studentId);
    }

    @Transactional
    public void deleteEnrollment(Long id) {

        repository.deleteById(id);
    }

    private EnrollmentResponse mapToResponse(
            Enrollment enrollment
    ) {

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudentId())
                .sectionId(enrollment.getSectionId())
                .status(enrollment.getStatus())
                .paymentReference(
                        enrollment.getPaymentReference()
                )
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}