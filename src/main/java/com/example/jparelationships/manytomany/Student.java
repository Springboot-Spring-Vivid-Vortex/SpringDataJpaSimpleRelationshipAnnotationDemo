package com.example.jparelationships.manytomany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * The owning side of a bidirectional {@code @ManyToMany} with {@link Course}.
 *
 * <p>Full write-up: {@code /doc/manytomany.md}
 *
 * <h2>Why a {@link Set}, not a {@link java.util.List}?</h2>
 * {@code @ManyToMany} collections should almost always be {@code Set}s.
 * Hibernate's default strategy for modifying a many-to-many collection is to
 * delete <i>every</i> row in the join table for that owner and re-insert the
 * current contents. A {@code List} additionally risks accidental duplicate
 * rows (a student "enrolled twice" in the same course) since lists allow
 * duplicates and don't dedupe by equality the way a {@code Set} does.
 */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * <ul>
     *   <li>{@code @JoinTable} - explicitly declares the join table
     *       ({@code student_course}) that links students to courses. Because
     *       this side declares {@code @JoinTable} (and the other side uses
     *       {@code mappedBy}), <b>this is the owning side</b>: Hibernate
     *       only writes to {@code student_course} when the {@code courses}
     *       field here is changed.</li>
     *   <li>{@code joinColumns} - the column(s) in the join table that point
     *       back to <i>this</i> entity ({@code Student}).</li>
     *   <li>{@code inverseJoinColumns} - the column(s) in the join table
     *       that point to the <i>other</i> entity ({@code Course}).</li>
     *   <li>{@code fetch = FetchType.LAZY} (the {@code @ManyToMany} default,
     *       spelled out for clarity) - courses are only loaded on demand.</li>
     * </ul>
     *
     * <p>{@code @JsonIgnoreProperties("students")} tells Jackson: when you
     * serialize the courses in this set, skip each course's own
     * {@code students} field. Without this, Jackson would try to serialize
     * Student -&gt; courses -&gt; students -&gt; courses -&gt; ... forever.
     * See {@code /doc/json-serialization.md}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonIgnoreProperties("students")
    private Set<Course> courses = new HashSet<>();

    protected Student() {
        // Required by JPA.
    }

    public Student(String name) {
        this.name = name;
    }

    /**
     * Keeps both sides of the in-memory object graph consistent. Only the
     * owning side ({@code this.courses}) affects the database, but a
     * consistent graph avoids surprises (e.g. before the objects are ever
     * saved/flushed).
     */
    public void enrollIn(Course course) {
        courses.add(course);
        course.getStudentsInternal().add(this);
    }

    public void unenrollFrom(Course course) {
        courses.remove(course);
        course.getStudentsInternal().remove(this);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    @Override
    public String toString() {
        return "Student{id=%d, name='%s'}".formatted(id, name);
    }
}
