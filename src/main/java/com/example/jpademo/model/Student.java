package com.example.jpademo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

/** A student owns the foreign keys for both of its relationships in this example. */
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // EAGER is the JPA default for to-one relations. LAZY is explicit here to avoid unnecessary queries.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", unique = true)
    private StudentProfile profile;

    // This is the owning side: it declares the join table and persists association changes.
    @ManyToMany
    @JoinTable(
            name = "student_course_enrollments",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new LinkedHashSet<>();

    protected Student() { }
    public Student(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public StudentProfile getProfile() { return profile; }
    public Set<Course> getCourses() { return courses; }

    public void setProfile(StudentProfile profile) {
        this.profile = profile;
        if (profile != null) profile.setStudent(this);
    }

    /** Keeps both in-memory sides in agreement; JPA only writes from this owning side. */
    public void enrollIn(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }
}
