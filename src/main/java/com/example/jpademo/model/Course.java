package com.example.jpademo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

/** A course belongs to one department and can have many enrolled students. */
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    // The MANY side owns this relationship because this courses table contains department_id.
    // Optional false turns the Java rule "a course needs a department" into a database NOT NULL rule.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new LinkedHashSet<>();

    protected Course() { }
    public Course(String title) { this.title = title; }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Department getDepartment() { return department; }
    public Set<Student> getStudents() { return students; }
    public void setDepartment(Department department) { this.department = department; }
}
