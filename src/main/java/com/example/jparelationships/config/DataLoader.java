package com.example.jparelationships.config;

import com.example.jparelationships.common.Address;
import com.example.jparelationships.manytomany.*;
import com.example.jparelationships.onetomany.Department;
import com.example.jparelationships.onetomany.DepartmentRepository;
import com.example.jparelationships.onetomany.Employee;
import com.example.jparelationships.onetoone.User;
import com.example.jparelationships.onetoone.UserProfile;
import com.example.jparelationships.onetoone.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Runs once on startup, after the schema has been created (see
 * {@code spring.jpa.hibernate.ddl-auto=create-drop}), to:
 *
 * <ol>
 *   <li>Seed a small, realistic dataset for every relationship type.</li>
 *   <li>Print a narrated walkthrough of each relationship to the console so
 *       you can see the generated SQL (enabled via
 *       {@code spring.jpa.show-sql=true}) right next to the Java code that
 *       triggered it.</li>
 * </ol>
 *
 * <p>Everything here runs inside a single {@code @Transactional} method so
 * lazy collections/associations can be navigated freely without hitting
 * {@code LazyInitializationException} - see {@code /doc/fetch-types.md}.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public DataLoader(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       StudentRepository studentRepository,
                       CourseRepository courseRepository,
                       CourseEnrollmentRepository courseEnrollmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        banner("1) @OneToOne  -  User <-> UserProfile");
        demoOneToOne();

        banner("2) @OneToMany / @ManyToOne  -  Department <-> Employee (+ @Embedded + @ElementCollection)");
        demoOneToMany();

        banner("3) @ManyToMany  -  Student <-> Course");
        demoManyToMany();

        banner("4) @EmbeddedId  -  CourseEnrollment (many-to-many with extra columns)");
        demoEmbeddedIdAssociation();
    }

    private void demoOneToOne() {
        User user = new User("ada");
        user.assignProfile(new UserProfile("Ada Lovelace", "Mathematician & writer"));
        userRepository.save(user);

        User reloaded = userRepository.findByUsername("ada").orElseThrow();
        log.info("Saved {} and its profile is loaded EAGERly: {}", reloaded, reloaded.getProfile());
        log.info("Navigating back from the inverse side: profile.getUser() = {}", reloaded.getProfile().getUser());
    }

    private void demoOneToMany() {
        Department engineering = new Department("Engineering");
        Employee grace = new Employee("Grace Hopper");
        grace.setHomeAddress(new Address("1 Compiler Way", "Arlington", "22201", "USA"));
        grace.addSkill("COBOL");
        grace.addSkill("Naval Engineering");
        engineering.addEmployee(grace);

        Employee linus = new Employee("Linus Torvalds");
        linus.setHomeAddress(new Address("2 Kernel Ave", "Portland", "97201", "USA"));
        linus.addSkill("C");
        linus.addSkill("Git");
        engineering.addEmployee(linus);

        // cascade = ALL on Department.employees means saving the department
        // also inserts both employee rows (and their embedded address /
        // element-collection skill rows) in one call.
        departmentRepository.save(engineering);

        Department reloaded = departmentRepository.findByIdWithEmployees(engineering.getId()).orElseThrow();
        log.info("Department {} has {} employees:", reloaded.getName(), reloaded.getEmployees().size());
        reloaded.getEmployees().forEach(e ->
                log.info("  - {} | address={} | skills={}", e.getName(), e.getHomeAddress(), e.getSkills()));

        // orphanRemoval demo: detach one employee from the collection and
        // save - Hibernate deletes that employee's row (and its skills)
        // even though we never called employeeRepository.delete(...).
        Employee toRemove = reloaded.getEmployees().get(0);
        reloaded.removeEmployee(toRemove);
        departmentRepository.save(reloaded);
        log.info("After orphanRemoval, department now has {} employee(s) left", reloaded.getEmployees().size());
    }

    private void demoManyToMany() {
        Student ada = new Student("Ada Lovelace");
        Student alan = new Student("Alan Turing");

        Course algorithms = new Course("Algorithms 101");
        Course cryptography = new Course("Cryptography 201");

        // Unlike Department.employees, Student.courses/Course.students has
        // NO cascade configured (see the "extra columns"/cascade note in
        // /doc/manytomany.md): courses are a shared catalog, not something
        // that should be created as a side effect of one student enrolling,
        // and definitely not something that should be *deleted* just
        // because one enrolled student is deleted. So both sides must
        // already be persisted (have an id) before we can link them -
        // linking two brand-new, never-saved entities here would throw
        // Hibernate's TransientObjectException.
        courseRepository.save(algorithms);
        courseRepository.save(cryptography);

        ada.enrollIn(algorithms);
        ada.enrollIn(cryptography);
        alan.enrollIn(cryptography);

        // Saving the owning side (Student) writes to the student_course
        // join table. Course.students is the mappedBy / inverse side.
        studentRepository.save(ada);
        studentRepository.save(alan);

        Course reloadedCrypto = courseRepository.findAll().stream()
                .filter(c -> c.getTitle().equals("Cryptography 201"))
                .findFirst().orElseThrow();
        log.info("{} has {} student(s) enrolled: {}", reloadedCrypto.getTitle(),
                reloadedCrypto.getStudents().size(), reloadedCrypto.getStudents());
    }

    private void demoEmbeddedIdAssociation() {
        Student bob = new Student("Bob Ross");
        Course painting = new Course("Happy Little Trees 101");
        studentRepository.save(bob);
        courseRepository.save(painting);

        CourseEnrollment enrollment = new CourseEnrollment(bob, painting, LocalDate.now());
        enrollment.setGrade("A+");
        courseEnrollmentRepository.save(enrollment);

        CourseEnrollment reloaded = courseEnrollmentRepository.findById(enrollment.getId()).orElseThrow();
        log.info("Loaded enrollment by composite key {}: {}", reloaded.getId(), reloaded);
    }

    private void banner(String title) {
        log.info("");
        log.info("================================================================");
        log.info("{}", title);
        log.info("================================================================");
    }
}
