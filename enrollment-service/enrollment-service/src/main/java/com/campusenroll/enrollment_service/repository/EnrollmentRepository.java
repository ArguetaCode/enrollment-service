package com.campusenroll.enrollment_service.repository;

import com.campusenroll.enrollment_service.entity.Enrollment;
import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Enrollment e where e.id = :id")
    Optional<Enrollment> findByIdForUpdate(@Param("id") Long id);
}
