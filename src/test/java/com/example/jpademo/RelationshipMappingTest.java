package com.example.jpademo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jpademo.model.Course;
import com.example.jpademo.model.Department;
import com.example.jpademo.model.Student;
import com.example.jpademo.model.StudentProfile;
import com.example.jpademo.repository.CourseRepository;
import com.example.jpademo.repository.DepartmentRepository;
import com.example.jpademo.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** Integration tests: Hibernate creates the schema and exercises the mappings against H2. */
@DataJpaTest
class RelationshipMappingTest {
    @Autowired StudentRepository students;
    @Autowired DepartmentRepository departments;
    @Autowired CourseRepository courses;

    @Test
    void oneToOne_cascadesProfileAndMaintainsBothSides() {
        Student student = new Student("Asha");
        student.setProfile(new StudentProfile("555-0100"));

        Student saved = students.saveAndFlush(student);

        assertThat(saved.getProfile().getId()).isNotNull();
        assertThat(saved.getProfile().getStudent()).isSameAs(saved);
    }

    @Test
    void oneToMany_cascadesNewCourseAndSetsForeignKeyOwner() {
        Department department = new Department("Computer Science");
        Course course = new Course("Databases");
        department.addCourse(course);

        Department saved = departments.saveAndFlush(department);

        assertThat(saved.getCourses()).hasSize(1);
        assertThat(saved.getCourses().getFirst().getDepartment()).isSameAs(saved);
    }

    @Test
    void manyToMany_createsEnrollmentThroughOwningStudentSide() {
        Course course = courses.saveAndFlush(new Course("Spring Data JPA"));
        Student student = new Student("Ravi");
        student.enrollIn(course);

        Student saved = students.saveAndFlush(student);

        assertThat(saved.getCourses()).containsExactly(course);
        assertThat(course.getStudents()).contains(saved);
    }

    @Test
    void orphanRemoval_deletesCourseWhenRemovedFromDepartmentCollection() {
        Department department = new Department("Math");
        Course course = new Course("Algebra");
        department.addCourse(course);
        Department saved = departments.saveAndFlush(department);
        Long courseId = saved.getCourses().getFirst().getId();

        saved.removeCourse(saved.getCourses().getFirst());
        departments.flush();

        assertThat(courses.findById(courseId)).isEmpty();
    }
}
