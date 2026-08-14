package com.example.jparelationships.onetomany;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    /**
     * A hand-written JPQL query using {@code JOIN FETCH} to eagerly load the
     * lazy {@code employees} collection in a *single* SQL query, avoiding
     * the "N+1 select" problem and any risk of
     * {@code LazyInitializationException} once the session closes. See the
     * "N+1 selects" section of {@code /doc/fetch-types.md}.
     */
    @Query("select d from Department d join fetch d.employees where d.id = :id")
    Optional<Department> findByIdWithEmployees(Long id);
}
