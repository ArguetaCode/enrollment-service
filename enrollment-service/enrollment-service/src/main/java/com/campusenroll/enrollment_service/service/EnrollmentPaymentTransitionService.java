package com.campusenroll.enrollment_service.service;

import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import com.campusenroll.enrollment_service.exception.BusinessException;
import com.campusenroll.enrollment_service.exception.ResourceNotFoundException;
import com.campusenroll.enrollment_service.integration.CourseServiceClient;
import com.campusenroll.enrollment_service.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentPaymentTransitionService {

    private final EnrollmentRepository repository;
    private final CourseServiceClient courseServiceClient;
    private final EntityManager entityManager;

    @Transactional
    public Enrollment approvePayment(Long enrollmentId, Long paymentId) {
        if (paymentId == null) {
            throw new BusinessException("No se puede confirmar una inscripcion sin pago valido");
        }
        return transition(enrollmentId, paymentId, EnrollmentStatus.CONFIRMED);
    }

    @Transactional
    public Enrollment failPayment(Long enrollmentId, Long paymentId) {
        return transition(enrollmentId, paymentId, EnrollmentStatus.PAYMENT_FAILED);
    }

    @Transactional
    public Enrollment failPaymentProcessing(Long enrollmentId) {
        return transition(enrollmentId, null, EnrollmentStatus.PAYMENT_FAILED);
    }

    private Enrollment transition(Long enrollmentId, Long paymentId, EnrollmentStatus targetStatus) {
        // The synchronous request may still hold its original PENDING_PAYMENT entity while
        // the RabbitMQ consumer has already committed the same payment result.
        entityManager.clear();
        Enrollment enrollment = repository.findByIdForUpdate(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscripcion no encontrada para pago " + paymentId
                ));

        if (enrollment.getStatus() == targetStatus) {
            verifySamePaymentOrAttachReference(enrollment, paymentId);
            return repository.save(enrollment);
        }

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    "Transicion de inscripcion no permitida: "
                            + enrollment.getStatus() + " -> " + targetStatus
            );
        }

        if (targetStatus == EnrollmentStatus.CONFIRMED) {
            courseServiceClient.confirmSeat(enrollment.getSectionId());
        } else {
            courseServiceClient.releaseSeat(enrollment.getSectionId());
        }

        enrollment.setStatus(targetStatus);
        enrollment.setPaymentReference(paymentId != null ? paymentId.toString() : null);
        return repository.save(enrollment);
    }

    private void verifySamePaymentOrAttachReference(Enrollment enrollment, Long paymentId) {
        if (paymentId == null) {
            return;
        }

        String paymentReference = paymentId.toString();
        if (enrollment.getPaymentReference() == null) {
            enrollment.setPaymentReference(paymentReference);
            return;
        }

        if (!enrollment.getPaymentReference().equals(paymentReference)) {
            throw new BusinessException("La inscripcion ya fue procesada con otro pago");
        }
    }
}
