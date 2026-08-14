package com.example.jparelationships.onetomany;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The "one" side ("parent") of a bidirectional {@code @OneToMany} /
 * {@code @ManyToOne} relationship with {@link Employee}.
 *
 * <p>Full write-up: {@code /doc/onetomany.md}
 *
 * <h2>The most important rule of {@code @OneToMany}</h2>
 * A plain {@code @OneToMany} is, by itself, always the <b>inverse</b>
 * (non-owning) side. The owning side - the one with the foreign key column -
 * is almost always the {@code @ManyToOne} on the other entity (see
 * {@link Employee#department}). That is why this field has
 * {@code mappedBy = "department"}: it points at the field name on
 * {@code Employee} that owns the relationship.
 *
 * <p>Skipping {@code mappedBy} here would make this a
 * <i>unidirectional</i> {@code @OneToMany}, which Hibernate implements with
 * an extra join table by default - almost never what you want when a
 * perfectly good foreign key column is available. See the "unidirectional
 * pitfall" section in {@code /doc/onetomany.md}.
 */
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * <ul>
     *   <li>{@code mappedBy = "department"} - this is the inverse side;
     *       the {@code employees.department_id} foreign key column
     *       (owned by {@link Employee#department}) is the single source of
     *       truth for this relationship.</li>
     *   <li>{@code fetch = FetchType.LAZY} (the {@code @OneToMany} default,
     *       spelled out here for clarity) - the {@code employees} list is
     *       <b>not</b> loaded from the database until it is first accessed.
     *       Accessing it outside of an open Hibernate session/transaction
     *       throws {@code LazyInitializationException} - see
     *       {@code /doc/fetch-types.md}.</li>
     *   <li>{@code cascade = CascadeType.ALL} - saving/deleting a
     *       {@code Department} cascades to its {@code Employee}s. Sensible
     *       here because an {@code Employee} without a {@code Department}
     *       is meaningless in this demo; think carefully before doing this
     *       in a real HR system (you may not want deleting a department to
     *       delete every employee row!).</li>
     *   <li>{@code orphanRemoval = true} - removing an {@code Employee} from
     *       this list (and saving) deletes its row, because an orphaned
     *       employee (no department) is not allowed in our model.</li>
     * </ul>
     *
     * <p>We expose this collection as an unmodifiable {@link List} (see
     * {@link #getEmployees()}) and only allow mutation through
     * {@link #addEmployee(Employee)} / {@link #removeEmployee(Employee)} so
     * both sides of the bidirectional link can never go out of sync.
     */
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Employee> employees = new ArrayList<>();

    protected Department() {
        // Required by JPA.
    }

    public Department(String name) {
        this.name = name;
    }

    /** Keeps both sides of the association consistent in memory. */
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDepartmentInternal(this);
    }

    /** Keeps both sides of the association consistent in memory. */
    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.setDepartmentInternal(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }

    @Override
    public String toString() {
        return "Department{id=%d, name='%s'}".formatted(id, name);
    }
}
