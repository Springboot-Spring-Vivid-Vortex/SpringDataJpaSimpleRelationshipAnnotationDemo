package com.example.jparelationships.onetomany;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Derived query method: Spring Data JPA turns this method name into
    // "select e from Employee e where e.department.id = ?1" automatically.
    List<Employee> findByDepartmentId(Long departmentId);
}
