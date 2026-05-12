package com.campusenroll.enrollment_service.repository;

import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndSectionId(
            Long studentId,
            Long sectionId
    );

    List<Enrollment> findByStudentId(
            Long studentId
    );

    List<Enrollment> findByStudentIdAndStatusIn(
            Long studentId,
            List<EnrollmentStatus> statuses
    );

    Optional<Enrollment> findByIdAndStudentId(
            Long id,
            Long studentId
    );
}
