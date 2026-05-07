package com.campusenroll.enrollment_service.entity;

import com.campusenroll.enrollment_service.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrollments",

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_section",

                        columnNames = {
                                "student_id",
                                "section_id"
                        }
                )
        },

        indexes = {

                @Index(
                        name = "idx_student",
                        columnList = "student_id"
                ),

                @Index(
                        name = "idx_section",
                        columnList = "section_id"
                ),

                @Index(
                        name = "idx_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "student_id",
            nullable = false
    )
    private Long studentId;

    @Column(
            name = "section_id",
            nullable = false
    )
    private Long sectionId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private EnrollmentStatus status;

    @Column(
            name = "payment_reference"
    )
    private String paymentReference;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at"
    )
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {

            this.status =
                    EnrollmentStatus.PENDING_PAYMENT;
        }
    }

    @PreUpdate
    public void preUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}