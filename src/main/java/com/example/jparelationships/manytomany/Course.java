package com.example.jparelationships.manytomany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * The inverse ({@code mappedBy}) side of the {@code @ManyToMany} with
 * {@link Student}.
 *
 * <p>Full write-up: {@code /doc/manytomany.md}
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    /**
     * {@code mappedBy = "courses"} matches the field name on the owning
     * side ({@link Student#courses}). This side never writes to the
     * {@code student_course} join table directly.
     */
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("courses")
    private Set<Student> students = new HashSet<>();

    protected Course() {
        // Required by JPA.
    }

    public Course(String title) {
        this.title = title;
    }

    /** Package-private escape hatch used only by {@link Student#enrollIn}. */
    Set<Student> getStudentsInternal() {
        return students;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Set<Student> getStudents() {
        return students;
    }

    @Override
    public String toString() {
        return "Course{id=%d, title='%s'}".formatted(id, title);
    }
}
