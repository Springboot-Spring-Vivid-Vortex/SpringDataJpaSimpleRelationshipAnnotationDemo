package com.example.jparelationships.onetomany;

import com.example.jparelationships.common.Address;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class OneToManyRelationshipTest {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void cascadeAllPersistsEmployeesWhenDepartmentIsSaved() {
        Department sales = new Department("Sales");
        sales.addEmployee(new Employee("Willy Loman"));
        sales.addEmployee(new Employee("Elmer Gantry"));

        departmentRepository.save(sales);

        assertThat(employeeRepository.findByDepartmentId(sales.getId())).hasSize(2);
    }

    @Test
    void embeddedAddressAndElementCollectionSkillsRoundTrip() {
        Department engineering = new Department("Engineering");
        Employee grace = new Employee("Grace Hopper");
        grace.setHomeAddress(new Address("1 Compiler Way", "Arlington", "22201", "USA"));
        grace.addSkill("COBOL");
        grace.addSkill("Naval Engineering");
        engineering.addEmployee(grace);
        departmentRepository.saveAndFlush(engineering);

        Employee reloaded = employeeRepository.findById(grace.getId()).orElseThrow();

        assertThat(reloaded.getHomeAddress().getCity()).isEqualTo("Arlington");
        assertThat(reloaded.getSkills()).containsExactlyInAnyOrder("COBOL", "Naval Engineering");
    }

    @Test
    void orphanRemovalDeletesEmployeeRowWhenRemovedFromDepartment() {
        Department dept = new Department("Temp Dept");
        Employee employee = new Employee("Temp Employee");
        dept.addEmployee(employee);
        departmentRepository.saveAndFlush(dept);
        Long employeeId = employee.getId();
        assertThat(employeeRepository.findById(employeeId)).isPresent();

        dept.removeEmployee(employee);
        departmentRepository.saveAndFlush(dept);

        assertThat(employeeRepository.findById(employeeId)).isEmpty();
    }

    @Test
    void manyToOneSideOwnsTheForeignKeyAndCannotBeNull() {
        Employee employeeWithoutDepartment = new Employee("Nobody");
        // department_id is NOT NULL (see @JoinColumn(nullable = false) on
        // Employee.department) - saving without a department must fail.
        assertThatThrownBy(() -> employeeRepository.saveAndFlush(employeeWithoutDepartment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void joinFetchQueryLoadsEmployeesInASingleQuery() {
        Department dept = new Department("Marketing");
        dept.addEmployee(new Employee("Peggy Olson"));
        departmentRepository.saveAndFlush(dept);

        Department reloaded = departmentRepository.findByIdWithEmployees(dept.getId()).orElseThrow();

        // No LazyInitializationException even without an open transaction
        // boundary around this assertion, because JOIN FETCH already
        // populated the collection.
        assertThat(reloaded.getEmployees()).hasSize(1);
    }
}
