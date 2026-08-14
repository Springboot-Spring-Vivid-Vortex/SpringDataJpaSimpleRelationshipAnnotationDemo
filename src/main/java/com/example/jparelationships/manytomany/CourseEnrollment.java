package com.example.jparelationships.manytomany;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * "Many-to-many with extra columns" pattern.
 *
 * <p>Full write-up: {@code /doc/manytomany.md} (section "extra columns on
 * the join table") and {@code /doc/embeddable.md} (section "@EmbeddedId").
 *
 * <h2>The problem with plain {@code @ManyToMany}</h2>
 * {@link Student} and {@link Course} use a plain {@code @ManyToMany} whose
 * join table ({@code student_course}) has <i>only</i> the two foreign keys.
 * That's fine until you need to record something about the enrollment
 * itself - an enrollment date, a grade, a status. A join table mapped only
 * via {@code @ManyToMany} cannot carry extra columns.
 *
 * <p>The standard fix is to <b>promote the join table to a real entity</b>:
 * turn "student enrolls in course" into a first-class
 * {@code CourseEnrollment} entity with two {@code @ManyToOne} associations
 * (one per side of the original many-to-many) plus whatever extra columns
 * you need. Its primary key is the combination of both foreign keys - modeled
 * here with the {@code @EmbeddedId} class {@link CourseEnrollmentId} - which
 * is exactly the composite primary key a hand-rolled join table would have
 * had anyway.
 *
 * <p>This example reuses the {@link Student} and {@link Course} entities
 * from the plain {@code @ManyToMany} demo above purely for convenience - in
 * a real application you would typically pick <b>either</b> the plain
 * {@code @ManyToMany} <b>or</b> this association-entity pattern for a given
 * relationship, not both.
 */
@Entity
@Table(name = "course_enrollments")
public class CourseEnrollment {

    /**
     * {@code @EmbeddedId} - the entire primary key is the embeddable
     * {@link CourseEnrollmentId}, not a single generated column. There is
     * no separate {@code @Id private Long id}.
     */
    @EmbeddedId
    private CourseEnrollmentId id;

    /**
     * {@code @MapsId("studentId")} tells Hibernate: "the foreign key column
     * for this association is *also* part of the primary key - specifically
     * the {@code studentId} field of {@link CourseEnrollmentId}". This
     * avoids duplicating the student's id in two separate columns; the
     * {@code student_id} column serves double duty as both a foreign key
     * and (part of) the primary key.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    /** An "extra column" that a plain join table could never have. */
    private LocalDate enrolledOn;

    /** Another extra column - e.g. filled in once the course is graded. */
    private String grade;

    protected CourseEnrollment() {
        // Required by JPA.
    }

    public CourseEnrollment(Student student, Course course, LocalDate enrolledOn) {
        this.student = student;
        this.course = course;
        this.enrolledOn = enrolledOn;
        this.id = new CourseEnrollmentId(student.getId(), course.getId());
    }

    public CourseEnrollmentId getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "CourseEnrollment{studentId=%d, courseId=%d, enrolledOn=%s, grade=%s}"
                .formatted(id.getStudentId(), id.getCourseId(), enrolledOn, grade);
    }
}
