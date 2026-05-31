package com.campusenroll.enrollment_service.service;

import com.campusenroll.enrollment_service.dto.EnrollmentRequest;
import com.campusenroll.enrollment_service.dto.EnrollmentResponse;
import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import com.campusenroll.enrollment_service.exception.BusinessException;
import com.campusenroll.enrollment_service.exception.ConflictException;
import com.campusenroll.enrollment_service.exception.ResourceNotFoundException;
import com.campusenroll.enrollment_service.integration.BillingServiceClient;
import com.campusenroll.enrollment_service.integration.CourseServiceClient;
import com.campusenroll.enrollment_service.integration.StudentServiceClient;
import com.campusenroll.enrollment_service.integration.dto.PaymentEvent;
import com.campusenroll.enrollment_service.integration.dto.PaymentRequest;
import com.campusenroll.enrollment_service.integration.dto.PaymentResponse;
import com.campusenroll.enrollment_service.integration.dto.SectionScheduleViewResponse;
import com.campusenroll.enrollment_service.integration.dto.StudentStatusResponse;
import com.campusenroll.enrollment_service.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final int PAYMENT_EVENT_WAIT_ATTEMPTS = 50;
    private static final long PAYMENT_EVENT_WAIT_MILLIS = 100L;

    private static final List<EnrollmentStatus> ACTIVE_ENROLLMENT_STATUSES = List.of(
            EnrollmentStatus.PENDING_PAYMENT,
            EnrollmentStatus.CONFIRMED
    );

    private final EnrollmentRepository repository;
    private final StudentServiceClient studentServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final BillingServiceClient billingServiceClient;
<<<<<<< Updated upstream
    private final EnrollmentPaymentTransitionService paymentTransitionService;
=======
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
>>>>>>> Stashed changes

    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        validateStudentActive(request.getStudentId());
        courseServiceClient.getSection(request.getSectionId());
        validateNotDuplicatedEnrollment(request.getStudentId(), request.getSectionId());
        validateScheduleOverlap(request.getStudentId(), request.getSectionId());

        boolean seatReserved = false;
        Enrollment saved = null;

        try {
            courseServiceClient.reserveSeat(request.getSectionId());
            seatReserved = true;

            Enrollment enrollment = Enrollment.builder()
                    .studentId(request.getStudentId())
                    .sectionId(request.getSectionId())
                    .status(EnrollmentStatus.PENDING_PAYMENT)
                    .build();
<<<<<<< Updated upstream
            // Commit PENDING_PAYMENT before billing validates and records its payment.
            saved = repository.save(enrollment);
=======
            saved = savePendingEnrollment(enrollment);
>>>>>>> Stashed changes

            PaymentResponse payment = billingServiceClient.processPayment(
                    new PaymentRequest(
                            saved.getId(),
                            saved.getStudentId(),
                            request.getAmount(),
                            request.isSimulatePaymentFailure()
                    )
            );

<<<<<<< Updated upstream
            return mapToResponse(applyPaymentResponse(saved, payment));
        } catch (DataIntegrityViolationException ex) {
            if (saved == null) {
                if (seatReserved) {
                    safeReleaseSeat(request.getSectionId());
                }
                throw new ConflictException("El estudiante ya tiene una inscripcion activa en esta seccion");
            }
            failPendingEnrollment(saved.getId());
            throw new BusinessException("Error procesando inscripcion y pago");
=======
            EnrollmentStatus expectedStatus = "APPROVED".equalsIgnoreCase(payment.status())
                    ? EnrollmentStatus.CONFIRMED
                    : EnrollmentStatus.PAYMENT_FAILED;
            return waitForPaymentEventResult(saved.getId(), expectedStatus);
>>>>>>> Stashed changes
        } catch (ResourceNotFoundException | BusinessException ex) {
            if (saved != null) {
                failPendingEnrollment(saved.getId());
            } else if (seatReserved) {
                safeReleaseSeat(request.getSectionId());
            }
            if (saved != null) {
                markEnrollmentCancelled(saved.getId());
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (saved != null) {
                failPendingEnrollment(saved.getId());
            } else if (seatReserved) {
                safeReleaseSeat(request.getSectionId());
            }
            if (saved != null) {
                markEnrollmentCancelled(saved.getId());
            }
            throw new BusinessException("Error procesando inscripcion y pago");
        }
    }

    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion no encontrada"));
        return mapToResponse(enrollment);
    }

    public List<EnrollmentResponse> getAllEnrollments() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<Enrollment> getStudentEnrollments(Long studentId) {
        return repository.findByStudentId(studentId);
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion no encontrada"));
        repository.deleteById(id);
    }

    @Transactional
    public void confirmEnrollmentPayment(PaymentEvent event) {
<<<<<<< Updated upstream
        validatePaymentEvent(event, "APPROVED");
        paymentTransitionService.approvePayment(event.enrollmentId(), event.paymentId());
=======
        Enrollment enrollment = findLockedEnrollmentFromPaymentEvent(event);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            return;
        }

        courseServiceClient.confirmSeat(enrollment.getSectionId());
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        enrollment.setPaymentReference(event.paymentId() != null ? event.paymentId().toString() : null);
>>>>>>> Stashed changes
    }

    @Transactional
    public void failEnrollmentPayment(PaymentEvent event) {
<<<<<<< Updated upstream
        validatePaymentEvent(event, "FAILED");
        paymentTransitionService.failPayment(event.enrollmentId(), event.paymentId());
=======
        Enrollment enrollment = findLockedEnrollmentFromPaymentEvent(event);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            return;
        }

        courseServiceClient.releaseSeat(enrollment.getSectionId());
        enrollment.setStatus(EnrollmentStatus.PAYMENT_FAILED);
        enrollment.setPaymentReference(event.paymentId() != null ? event.paymentId().toString() : null);
>>>>>>> Stashed changes
    }

    private void validateStudentActive(Long studentId) {
        StudentStatusResponse status = studentServiceClient.getStudentStatus(studentId);
        if (!status.active()) {
            throw new BusinessException("El estudiante esta inactivo o bloqueado");
        }
    }

    private void validateNotDuplicatedEnrollment(Long studentId, Long sectionId) {
        List<Enrollment> existing = repository.findByStudentIdAndStatusIn(studentId, ACTIVE_ENROLLMENT_STATUSES);
        boolean duplicated = existing.stream().anyMatch(e -> sectionId.equals(e.getSectionId()));
        if (duplicated) {
            throw new ConflictException("El estudiante ya tiene una inscripcion activa en esta seccion");
        }
    }

    private void validateScheduleOverlap(Long studentId, Long sectionId) {
        List<SectionScheduleViewResponse> requestedSchedule = courseServiceClient.getSectionSchedule(sectionId);
        if (requestedSchedule.isEmpty()) {
            return;
        }

        List<Enrollment> existing = repository.findByStudentIdAndStatusIn(studentId, ACTIVE_ENROLLMENT_STATUSES);
        for (Enrollment enrollment : existing) {
            List<SectionScheduleViewResponse> existingSchedule = courseServiceClient.getSectionSchedule(enrollment.getSectionId());
            if (hasOverlap(requestedSchedule, existingSchedule)) {
                throw new BusinessException("La seccion solicitada tiene traslape de horario con otra inscripcion activa");
            }
        }
    }

    private boolean hasOverlap(
            List<SectionScheduleViewResponse> requested,
            List<SectionScheduleViewResponse> existing
    ) {
        for (SectionScheduleViewResponse left : requested) {
            for (SectionScheduleViewResponse right : existing) {
                boolean sameDay = left.dayOfWeek().equalsIgnoreCase(right.dayOfWeek());
                boolean overlaps = left.startTime().isBefore(right.endTime())
                        && left.endTime().isAfter(right.startTime());
                if (sameDay && overlaps) {
                    return true;
                }
            }
        }
        return false;
    }

    private void safeReleaseSeat(Long sectionId) {
        try {
            courseServiceClient.releaseSeat(sectionId);
        } catch (RuntimeException ignored) {
            // Intentionally ignored to preserve original exception and avoid masking failures.
        }
    }

<<<<<<< Updated upstream
    private Enrollment applyPaymentResponse(Enrollment enrollment, PaymentResponse payment) {
        if (payment == null
                || payment.paymentId() == null
                || !enrollment.getId().equals(payment.enrollmentId())
                || !enrollment.getStudentId().equals(payment.studentId())) {
            throw new BusinessException("Billing devolvio una respuesta de pago invalida");
        }

        if ("APPROVED".equalsIgnoreCase(payment.status())) {
            return paymentTransitionService.approvePayment(enrollment.getId(), payment.paymentId());
        }
        if ("FAILED".equalsIgnoreCase(payment.status())) {
            return paymentTransitionService.failPayment(enrollment.getId(), payment.paymentId());
        }
        throw new BusinessException("Estado de pago no permitido: " + payment.status());
    }

    private void validatePaymentEvent(PaymentEvent event, String expectedStatus) {
        if (event == null || event.paymentId() == null || event.enrollmentId() == null) {
            throw new BusinessException("Evento de pago invalido");
        }
        if (!expectedStatus.equalsIgnoreCase(event.status())) {
            throw new BusinessException("Evento de pago no coincide con la transicion solicitada");
        }
    }

    private void failPendingEnrollment(Long enrollmentId) {
        try {
            paymentTransitionService.failPaymentProcessing(enrollmentId);
        } catch (BusinessException ignored) {
            // A terminal transition already won the race with this recovery path.
        }
=======
    private Enrollment savePendingEnrollment(Enrollment enrollment) {
        return transactionTemplate.execute(status -> repository.save(enrollment));
    }

    private void markEnrollmentCancelled(Long enrollmentId) {
        transactionTemplate.executeWithoutResult(status -> repository.findById(enrollmentId).ifPresent(enrollment -> {
            enrollment.setStatus(EnrollmentStatus.CANCELLED);
            repository.save(enrollment);
        }));
    }

    private EnrollmentResponse waitForPaymentEventResult(Long enrollmentId, EnrollmentStatus expectedStatus) {
        for (int attempt = 0; attempt < PAYMENT_EVENT_WAIT_ATTEMPTS; attempt++) {
            entityManager.clear();
            Enrollment enrollment = repository.findById(enrollmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inscripcion no encontrada"));

            if (enrollment.getStatus() == expectedStatus
                    || enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
                return mapToResponse(enrollment);
            }

            waitBeforeNextPaymentStatusCheck();
        }

        return getEnrollmentById(enrollmentId);
    }

    private void waitBeforeNextPaymentStatusCheck() {
        try {
            Thread.sleep(PAYMENT_EVENT_WAIT_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Espera interrumpida al confirmar pago");
        }
    }

    private Enrollment findLockedEnrollmentFromPaymentEvent(PaymentEvent event) {
        return repository.findByIdForUpdate(event.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscripcion no encontrada para pago " + event.paymentId()
                ));
>>>>>>> Stashed changes
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudentId())
                .sectionId(enrollment.getSectionId())
                .status(enrollment.getStatus())
                .paymentReference(enrollment.getPaymentReference())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}
