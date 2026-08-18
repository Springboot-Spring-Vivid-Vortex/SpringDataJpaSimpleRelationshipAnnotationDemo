package com.example.jpademo.repository;

import com.example.jpademo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

/** JpaRepository adds CRUD, paging, sorting, flush, and batch operations for Student. */
public interface StudentRepository extends JpaRepository<Student, Long> { }
