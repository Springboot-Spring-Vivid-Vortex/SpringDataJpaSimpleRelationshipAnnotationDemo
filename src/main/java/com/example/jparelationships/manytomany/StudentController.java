package com.example.jparelationships.manytomany;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Try: {@code GET http://localhost:8080/api/students}
 * and: {@code GET http://localhost:8080/api/students/courses} for the
 * inverse view.
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentController(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Student> findAllStudents() {
        List<Student> students = studentRepository.findAll();
        students.forEach(s -> s.getCourses().size()); // force-initialize lazy set
        return students;
    }

    @GetMapping("/courses")
    @Transactional(readOnly = true)
    public List<Course> findAllCourses() {
        List<Course> courses = courseRepository.findAll();
        courses.forEach(c -> c.getStudents().size());
        return courses;
    }
}
