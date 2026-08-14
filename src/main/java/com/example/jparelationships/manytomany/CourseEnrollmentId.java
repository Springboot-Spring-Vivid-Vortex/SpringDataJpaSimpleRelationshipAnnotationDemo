package com.example.jparelationships.manytomany;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link CourseEnrollment}.
 *
 * <p>Full write-up: {@code /doc/embeddable.md} (section "@EmbeddedId").
 *
 * <h2>Rules for an {@code @EmbeddedId} class</h2>
 * <ul>
 *   <li>Must implement {@link Serializable}.</li>
 *   <li>Must override {@link #equals(Object)} and {@link #hashCode()} based
 *       on <b>all</b> fields - Hibernate uses these to identify rows in its
 *       first-level cache and to compare keys.</li>
 *   <li>Must have a no-arg constructor.</li>
 * </ul>
 */
@Embeddable
public class CourseEnrollmentId implements Serializable {

    private Long studentId;
    private Long courseId;

    protected CourseEnrollmentId() {
        // Required by JPA.
    }

    public CourseEnrollmentId(Long studentId, Long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseEnrollmentId that)) return false;
        return Objects.equals(studentId, that.studentId) && Objects.equals(courseId, that.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseId);
    }
}
