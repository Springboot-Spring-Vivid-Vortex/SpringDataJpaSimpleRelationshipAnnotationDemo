package com.example.jparelationships.manytomany;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ManyToManyRelationshipTest {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Test
    void savingTheOwningSidePopulatesTheJoinTableForBothDirections() {
        Student ada = new Student("Ada Lovelace");
        Course algorithms = new Course("Algorithms 101");
        // No cascade on @ManyToMany (see /doc/manytomany.md) - both sides
        // must already be persisted before they can be linked.
        courseRepository.saveAndFlush(algorithms);
        ada.enrollIn(algorithms);

        studentRepository.saveAndFlush(ada);

        Course reloadedCourse = courseRepository.findById(algorithms.getId()).orElseThrow();
        assertThat(reloadedCourse.getStudents()).extracting(Student::getName).containsExactly("Ada Lovelace");
    }

    @Test
    void oneStudentCanEnrollInMultipleCoursesAndViceVersa() {
        Student ada = new Student("Ada Lovelace");
        Student alan = new Student("Alan Turing");
        Course algorithms = new Course("Algorithms 101");
        Course cryptography = new Course("Cryptography 201");
        courseRepository.saveAndFlush(algorithms);
        courseRepository.saveAndFlush(cryptography);

        ada.enrollIn(algorithms);
        ada.enrollIn(cryptography);
        alan.enrollIn(cryptography);
        studentRepository.saveAndFlush(ada);
        studentRepository.saveAndFlush(alan);

        assertThat(ada.getCourses()).hasSize(2);
        Course reloadedCrypto = courseRepository.findById(cryptography.getId()).orElseThrow();
        assertThat(reloadedCrypto.getStudents()).hasSize(2);
    }

    @Test
    void embeddedIdAssociationStoresExtraColumnsOnTheJoinRow() {
        Student bob = new Student("Bob Ross");
        Course painting = new Course("Happy Little Trees 101");
        studentRepository.saveAndFlush(bob);
        courseRepository.saveAndFlush(painting);

        CourseEnrollment enrollment = new CourseEnrollment(bob, painting, LocalDate.of(2024, 1, 15));
        enrollment.setGrade("A+");
        courseEnrollmentRepository.saveAndFlush(enrollment);

        CourseEnrollmentId key = new CourseEnrollmentId(bob.getId(), painting.getId());
        CourseEnrollment reloaded = courseEnrollmentRepository.findById(key).orElseThrow();

        assertThat(reloaded.getGrade()).isEqualTo("A+");
        assertThat(reloaded.getEnrolledOn()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(reloaded.getStudent().getName()).isEqualTo("Bob Ross");
        assertThat(reloaded.getCourse().getTitle()).isEqualTo("Happy Little Trees 101");
    }
}
