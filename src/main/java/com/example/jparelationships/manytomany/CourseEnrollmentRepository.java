package com.example.jparelationships.manytomany;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Note the id type here is {@link CourseEnrollmentId} - the
 * {@code @EmbeddedId} class - not {@code Long}.
 */
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, CourseEnrollmentId> {
}
