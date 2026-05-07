package com.campusenroll.enrollment_service.service;

import com.campusenroll.enrollment_service.dto.EnrollmentRequest;
import com.campusenroll.enrollment_service.dto.EnrollmentResponse;
import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import com.campusenroll.enrollment_service.integration.BillingServiceClient;
import com.campusenroll.enrollment_service.integration.CourseServiceClient;
import com.campusenroll.enrollment_service.integration.StudentServiceClient;
import com.campusenroll.enrollment_service.integration.dto.PaymentEvent;
import com.campusenroll.enrollment_service.integration.dto.PaymentRequest;
import com.campusenroll.enrollment_service.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository repository;
    private final StudentServiceClient studentServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final BillingServiceClient billingServiceClient;

    public EnrollmentResponse createEnrollment(
            EnrollmentRequest request
    ) {

        studentServiceClient.validateStudentExists(request.getStudentId());
        courseServiceClient.validateSectionExists(request.getSectionId());

        boolean exists =
                repository.existsByStudentIdAndSectionId(
                        request.getStudentId(),
                        request.getSectionId()
                );

        if (exists) {
            throw new RuntimeException(
                    "El estudiante ya esta inscrito"
            );
        }

        boolean seatReserved = false;

        try {
            courseServiceClient.reserveSeat(request.getSectionId());
            seatReserved = true;

            Enrollment enrollment =
                    Enrollment.builder()
                            .studentId(request.getStudentId())
                            .sectionId(request.getSectionId())
                            .build();

            Enrollment saved =
                    repository.save(enrollment);

            billingServiceClient.processPayment(
                    new PaymentRequest(
                            saved.getId(),
                            saved.getStudentId(),
                            request.getAmount(),
                            request.isSimulatePaymentFailure()
                    )
            );

            return mapToResponse(saved);
        } catch (RuntimeException ex) {
            if (seatReserved) {
                courseServiceClient.releaseSeat(request.getSectionId());
            }

            throw ex;
        }
    }

    public EnrollmentResponse getEnrollmentById(
            Long id
    ) {

        Enrollment enrollment =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inscripcion no encontrada"
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

    @Transactional
    public void confirmEnrollmentPayment(PaymentEvent event) {

        Enrollment enrollment =
                findEnrollmentFromPaymentEvent(event);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            return;
        }

        courseServiceClient.confirmSeat(enrollment.getSectionId());

        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        enrollment.setPaymentReference(event.paymentId().toString());
    }

    @Transactional
    public void failEnrollmentPayment(PaymentEvent event) {

        Enrollment enrollment =
                findEnrollmentFromPaymentEvent(event);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            return;
        }

        courseServiceClient.releaseSeat(enrollment.getSectionId());

        enrollment.setStatus(EnrollmentStatus.PAYMENT_FAILED);
        enrollment.setPaymentReference(event.paymentId().toString());
    }

    private Enrollment findEnrollmentFromPaymentEvent(
            PaymentEvent event
    ) {

        return repository.findById(event.enrollmentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inscripcion no encontrada para pago "
                                        + event.paymentId()
                        )
                );
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
