package com.campusenroll.enrollment_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import com.campusenroll.enrollment_service.exception.BusinessException;
import com.campusenroll.enrollment_service.integration.CourseServiceClient;
import com.campusenroll.enrollment_service.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentPaymentTransitionServiceTest {

    @Mock
    private EnrollmentRepository repository;

    @Mock
    private CourseServiceClient courseServiceClient;

    @Mock
    private EntityManager entityManager;

    private EnrollmentPaymentTransitionService service;

    @BeforeEach
    void setUp() {
        service = new EnrollmentPaymentTransitionService(repository, courseServiceClient, entityManager);
    }

    @Test
    void confirmsPendingEnrollmentWithApprovedPayment() {
        Enrollment enrollment = pendingEnrollment();
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment));
        when(repository.save(enrollment)).thenReturn(enrollment);

        Enrollment updated = service.approvePayment(5L, 100L);

        assertEquals(EnrollmentStatus.CONFIRMED, updated.getStatus());
        assertEquals("100", updated.getPaymentReference());
        verify(courseServiceClient).confirmSeat(9L);
    }

    @Test
    void repeatedApprovedPaymentIsIdempotent() {
        Enrollment enrollment = pendingEnrollment();
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        enrollment.setPaymentReference("100");
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment));
        when(repository.save(enrollment)).thenReturn(enrollment);

        service.approvePayment(5L, 100L);

        verify(courseServiceClient, never()).confirmSeat(9L);
    }

    @Test
    void rejectsContradictoryPaymentTransition() {
        Enrollment enrollment = pendingEnrollment();
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        enrollment.setPaymentReference("100");
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.failPayment(5L, 101L)
        );

        assertEquals(
                "Transicion de inscripcion no permitida: CONFIRMED -> PAYMENT_FAILED",
                exception.getMessage()
        );
        verify(courseServiceClient, never()).releaseSeat(9L);
    }

    private Enrollment pendingEnrollment() {
        return Enrollment.builder()
                .id(5L)
                .studentId(7L)
                .sectionId(9L)
                .status(EnrollmentStatus.PENDING_PAYMENT)
                .build();
    }
}
