package com.example.jpademo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** The inverse side has no column of its own: mappedBy points to Student.profile. */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;

    @OneToOne(mappedBy = "profile")
    private Student student;

    protected StudentProfile() { }
    public StudentProfile(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
}
