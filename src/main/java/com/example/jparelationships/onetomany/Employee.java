package com.example.jparelationships.onetomany;

import com.example.jparelationships.common.Address;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The "many" side ("child") of the {@code Department} &lt;-&gt; {@code Employee}
 * relationship, and <b>the owning side</b> of it.
 *
 * <p>Full write-up: {@code /doc/onetomany.md}. This entity also demonstrates
 * {@code @Embedded} ({@code /doc/embeddable.md}) and {@code @ElementCollection}
 * ({@code /doc/elementcollection.md}).
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * The owning side of the {@code @ManyToOne}/{@code @OneToMany}
     * relationship: this field is what actually creates the
     * {@code department_id} foreign-key column on the {@code employees}
     * table.
     *
     * <ul>
     *   <li>{@code fetch = FetchType.LAZY} - overrides the
     *       {@code @ManyToOne} default of {@code EAGER}. This is one of the
     *       most common "gotchas" in JPA: unlike {@code @OneToMany}/
     *       {@code @ManyToMany}, {@code @ManyToOne} and {@code @OneToOne}
     *       default to {@code EAGER}. Explicitly setting {@code LAZY} here
     *       avoids accidentally loading the whole department graph every
     *       time a single employee is fetched. See {@code /doc/fetch-types.md}.</li>
     *   <li>{@code @JoinColumn(name = "department_id")} - names the foreign
     *       key column. Without this annotation Hibernate still creates the
     *       column, just with a default name.</li>
     * </ul>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonBackReference
    private Department department;

    /**
     * {@code @Embedded} pulls the columns declared in {@link Address}
     * (street, city, postal_code, country) directly into this table's row -
     * there is no separate "addresses" table. See {@code /doc/embeddable.md}.
     */
    @Embedded
    private Address homeAddress;

    /**
     * {@code @ElementCollection} maps a collection of a <i>basic</i> (or
     * {@code @Embeddable}) type - here, plain {@code String}s - to its own
     * table ({@code employee_skills}), without needing a full {@code @Entity}
     * class for "skill". Each row in that table is just
     * {@code (employee_id, skill)}.
     *
     * <p>Full write-up: {@code /doc/elementcollection.md}. Note this is
     * <b>not</b> a relationship between two entities - "skill" has no
     * identity, no repository, and cannot be fetched on its own; it only
     * ever exists as part of an {@code Employee}.
     */
    @ElementCollection
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    protected Employee() {
        // Required by JPA.
    }

    public Employee(String name) {
        this.name = name;
    }

    /** Package-private - mutated only through {@link Department#addEmployee}. */
    void setDepartmentInternal(Department department) {
        this.department = department;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public void addSkill(String skill) {
        skills.add(skill);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Department getDepartment() {
        return department;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public List<String> getSkills() {
        return Collections.unmodifiableList(skills);
    }

    @Override
    public String toString() {
        return "Employee{id=%d, name='%s'}".formatted(id, name);
    }
}
