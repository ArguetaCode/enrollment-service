package com.campusenroll.enrollment_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusenroll.enrollment_service.dto.EnrollmentRequest;
import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import com.campusenroll.enrollment_service.exception.BusinessException;
import com.campusenroll.enrollment_service.exception.ConflictException;
import com.campusenroll.enrollment_service.integration.BillingServiceClient;
import com.campusenroll.enrollment_service.integration.CourseServiceClient;
import com.campusenroll.enrollment_service.integration.StudentServiceClient;
import com.campusenroll.enrollment_service.integration.dto.CourseSectionResponse;
import com.campusenroll.enrollment_service.integration.dto.SectionScheduleViewResponse;
import com.campusenroll.enrollment_service.integration.dto.StudentStatusResponse;
import com.campusenroll.enrollment_service.repository.EnrollmentRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceRulesTest {

    @Mock
    private EnrollmentRepository repository;

    @Mock
    private StudentServiceClient studentServiceClient;

    @Mock
    private CourseServiceClient courseServiceClient;

    @Mock
    private BillingServiceClient billingServiceClient;

    @Mock
    private EnrollmentPaymentTransitionService paymentTransitionService;

    private EnrollmentService service;

    @BeforeEach
    void setUp() {
        service = new EnrollmentService(
                repository,
                studentServiceClient,
                courseServiceClient,
                billingServiceClient,
                paymentTransitionService
        );
        when(studentServiceClient.getStudentStatus(7L))
                .thenReturn(new StudentStatusResponse(7L, "ACTIVE", true));
        when(courseServiceClient.getSection(9L)).thenReturn(new CourseSectionResponse(9L));
    }

    @Test
    void rejectsActiveDuplicateEnrollmentBeforeReservingSeat() {
        Enrollment existing = Enrollment.builder()
                .studentId(7L)
                .sectionId(9L)
                .status(EnrollmentStatus.CONFIRMED)
                .build();
        when(repository.findByStudentIdAndStatusIn(eq(7L), anyList())).thenReturn(List.of(existing));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.createEnrollment(request())
        );

        assertEquals("El estudiante ya tiene una inscripcion activa en esta seccion", exception.getMessage());
        verify(courseServiceClient, never()).reserveSeat(9L);
    }

    @Test
    void propagatesCapacityConflictWithoutCreatingEnrollment() {
        when(repository.findByStudentIdAndStatusIn(eq(7L), anyList())).thenReturn(List.of());
        when(courseServiceClient.getSectionSchedule(9L)).thenReturn(List.of());
        org.mockito.Mockito.doThrow(new ConflictException("La seccion no tiene cupos disponibles: 9"))
                .when(courseServiceClient).reserveSeat(9L);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.createEnrollment(request())
        );

        assertEquals("La seccion no tiene cupos disponibles: 9", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void releasesSeatWhenConcurrentDuplicateIsRejectedByDatabase() {
        when(repository.findByStudentIdAndStatusIn(eq(7L), anyList())).thenReturn(List.of());
        when(courseServiceClient.getSectionSchedule(9L)).thenReturn(List.of());
        when(repository.save(any(Enrollment.class)))
                .thenThrow(new DataIntegrityViolationException("active duplicate"));

        assertThrows(ConflictException.class, () -> service.createEnrollment(request()));

        verify(courseServiceClient).reserveSeat(9L);
        verify(courseServiceClient).releaseSeat(9L);
        verify(billingServiceClient, never()).processPayment(any());
    }

    @Test
    void rejectsScheduleOverlapBeforeReservingSeat() {
        Enrollment current = Enrollment.builder()
                .studentId(7L)
                .sectionId(10L)
                .status(EnrollmentStatus.CONFIRMED)
                .build();
        when(repository.findByStudentIdAndStatusIn(eq(7L), anyList()))
                .thenReturn(List.of(), List.of(current));
        when(courseServiceClient.getSectionSchedule(9L)).thenReturn(List.of(
                new SectionScheduleViewResponse("MONDAY", LocalTime.of(9, 0), LocalTime.of(11, 0))
        ));
        when(courseServiceClient.getSectionSchedule(10L)).thenReturn(List.of(
                new SectionScheduleViewResponse("MONDAY", LocalTime.of(10, 0), LocalTime.of(12, 0))
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createEnrollment(request())
        );

        assertEquals("La seccion solicitada tiene traslape de horario con otra inscripcion activa", exception.getMessage());
        verify(courseServiceClient, never()).reserveSeat(9L);
    }

    private EnrollmentRequest request() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(7L);
        request.setSectionId(9L);
        request.setAmount(new BigDecimal("100.00"));
        return request;
    }
}
