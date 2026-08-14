package com.example.jparelationships.onetomany;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Try: {@code GET http://localhost:8080/api/departments}
 * The response includes each department's lazily-loaded {@code employees}
 * list, initialized here inside {@code @Transactional} - see
 * {@code /doc/fetch-types.md} for why that matters.
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Department> findAll() {
        List<Department> departments = departmentRepository.findAll();
        // Touch the lazy collection while the session is still open so
        // Jackson (which serializes the response *after* this method
        // returns, outside the transaction) doesn't hit a
        // LazyInitializationException.
        departments.forEach(d -> d.getEmployees().size());
        return departments;
    }

    @GetMapping("/{id}")
    public Department findById(@PathVariable Long id) {
        return departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new IllegalArgumentException("No department with id " + id));
    }
}
