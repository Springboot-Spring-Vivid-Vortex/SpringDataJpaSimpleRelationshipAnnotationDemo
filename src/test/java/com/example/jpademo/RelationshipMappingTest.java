package com.example.jpademo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jpademo.model.Course;
import com.example.jpademo.model.Department;
import com.example.jpademo.model.Student;
import com.example.jpademo.model.StudentProfile;
import com.example.jpademo.repository.CourseRepository;
import com.example.jpademo.repository.DepartmentRepository;
import com.example.jpademo.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** Integration tests: Hibernate creates the schema and exercises the mappings against H2. */
@DataJpaTest
class RelationshipMappingTest {
    @Autowired StudentRepository students;
    @Autowired DepartmentRepository departments;
    @Autowired CourseRepository courses;
    @Autowired EntityManager entityManager;

    @Test
    void oneToOne_cascadesProfileAndMaintainsBothSides() {
        Student student = new Student("Asha");
        student.setProfile(new StudentProfile("555-0100"));

        Student saved = students.saveAndFlush(student);

        assertThat(saved.getProfile().getId()).isNotNull();
        assertThat(saved.getProfile().getStudent()).isSameAs(saved);
    }

    @Test
    void oneToMany_cascadesNewCoursesAndSetsTheirManyToOneForeignKey() {
        Department department = new Department("Computer Science");
        department.addCourse(new Course("Databases"));
        department.addCourse(new Course("Java Basics"));

        Department saved = departments.saveAndFlush(department);

        assertThat(saved.getCourses()).hasSize(2);
        assertThat(saved.getCourses()).allSatisfy(course ->
                assertThat(course.getDepartment()).isSameAs(saved));
    }

    @Test
    void manyToOne_isOwnedByCourseAndStoresTheDepartmentForeignKey() {
        Department department = departments.saveAndFlush(new Department("Physics"));
        Course course = new Course("Mechanics");

        // We change the owning MANY side directly. This is what writes courses.department_id.
        course.setDepartment(department);
        Course savedCourse = courses.saveAndFlush(course);
        entityManager.clear(); // Prove the association was stored in H2, not just held in Java memory.

        Course reloaded = courses.findById(savedCourse.getId()).orElseThrow();
        assertThat(reloaded.getDepartment().getId()).isEqualTo(department.getId());
    }

    @Test
    void manyToMany_createsEnrollmentThroughOwningStudentSide() {
        Department department = new Department("Software Engineering");
        Course course = new Course("Spring Data JPA");
        department.addCourse(course);
        departments.saveAndFlush(department);

        Student ravi = new Student("Ravi");
        Student maya = new Student("Maya");
        ravi.enrollIn(course);
        maya.enrollIn(course);

        students.saveAndFlush(ravi);
        students.saveAndFlush(maya);
        entityManager.clear();

        Course reloadedCourse = courses.findById(course.getId()).orElseThrow();
        assertThat(reloadedCourse.getStudents()).extracting(Student::getName)
                .containsExactlyInAnyOrder("Ravi", "Maya");
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
