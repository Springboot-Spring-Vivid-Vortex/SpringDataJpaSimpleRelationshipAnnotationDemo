# `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@MapsId`

These four annotations are about **grouping columns**, not linking rows
across tables. Nothing here creates a foreign key or a second table.

## `@Embeddable` + `@Embedded` - flattening a value object into a row

**Example:** [`Address`](../src/main/java/com/example/jparelationships/common/Address.java),
embedded into [`Employee`](../src/main/java/com/example/jparelationships/onetomany/Employee.java)

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String postalCode;
    private String country;
}
```

```java
@Entity
public class Employee {
    @Embedded
    private Address homeAddress;
}
```

### The schema this produces

```
employees
+----+--------------+-------------+-----------+-------------+---------+
| id | name         | street      | city      | postal_code | country |
+----+--------------+-------------+-----------+-------------+---------+
| 1  | Grace Hopper | 1 Compiler..| Arlington | 22201       | USA     |
+----+--------------+-------------+-----------+-------------+---------+
```

**No `addresses` table exists.** `Address`'s four fields become four plain
columns directly on `employees`. `@Embeddable` marks a class that has:

- No identity of its own (no `@Id`).
- No lifecycle of its own - it's created/deleted exactly when its owning
  entity row is created/deleted; you can't `save()` or `delete()` an
  `Address` independently, and there's no `AddressRepository`.

It exists purely so Java code can group related fields (`street`, `city`,
`postalCode`, `country`) into a reusable, meaningfully-named type instead
of scattering four loose `String` fields across every entity that needs an
address.

### `@OneToOne` vs `@Embedded` - how to choose

| | `@OneToOne` | `@Embedded` |
|---|---|---|
| Separate table? | Yes | No - same row |
| Has its own id? | Yes | No |
| Can be shared/referenced by multiple owners? | Yes (in principle) | No - copied per owner |
| Can exist without the owner? | Yes (unless cascade removes it) | No |
| Query it independently? | Yes, via its own repository | No |

Rule of thumb: if the "thing" only ever makes sense as *part of* one
specific owner and nothing ever needs to reference it independently
(an address, a date range, a monetary amount with its currency), use
`@Embeddable`. If it has its own identity/lifecycle or other entities need
to reference it too, use a real relationship annotation instead.

### Nuance: embedding the same type twice (`@AttributeOverrides`)

If `Employee` needed *two* addresses (e.g. `homeAddress` and
`workAddress`), both `@Embedded Address` fields would otherwise fight over
the same column names (`street`, `city`, ...). Rename them per-field with:

```java
@Embedded
@AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column(name = "work_street")),
    @AttributeOverride(name = "city", column = @Column(name = "work_city"))
})
private Address workAddress;
```

## `@EmbeddedId` - composite primary keys

**Example:** [`CourseEnrollmentId`](../src/main/java/com/example/jparelationships/manytomany/CourseEnrollmentId.java),
used as the `@Id` of [`CourseEnrollment`](../src/main/java/com/example/jparelationships/manytomany/CourseEnrollment.java)

Some tables' natural primary key is a *combination* of columns - here,
"one row per (student, course) pair":

```java
@Embeddable
public class CourseEnrollmentId implements Serializable {
    private Long studentId;
    private Long courseId;
    // must override equals()/hashCode() using BOTH fields
}
```

```java
@Entity
public class CourseEnrollment {
    @EmbeddedId
    private CourseEnrollmentId id;
}
```

Requirements for a class used as `@EmbeddedId`:

1. Annotated `@Embeddable`.
2. Implements `Serializable`.
3. Has a no-arg constructor.
4. Overrides `equals()`/`hashCode()` using **every** key field - Hibernate
   uses these to identify entities in its session cache, so getting this
   wrong causes very confusing "duplicate" or "not found" bugs.

Repositories for entities with a composite key use the embeddable type as
the id type parameter:

```java
public interface CourseEnrollmentRepository
        extends JpaRepository<CourseEnrollment, CourseEnrollmentId> { }
```

## `@MapsId` - reusing a `@ManyToOne`'s foreign key as (part of) the primary key

```java
@ManyToOne
@MapsId("studentId")
@JoinColumn(name = "student_id")
private Student student;
```

Without `@MapsId`, `CourseEnrollment` would need a *separate*
`student_id` column for the `@ManyToOne` foreign key, on top of the
`student_id` already inside `CourseEnrollmentId` - two columns storing the
same value. `@MapsId("studentId")` tells Hibernate: "the foreign key
column for this `@ManyToOne` **is** the `studentId` field of my
`@EmbeddedId` - reuse it, don't duplicate it." The string
`"studentId"` must match the field name inside `CourseEnrollmentId`
exactly.

This is also usable with a *single-column* id (no `@EmbeddedId`) for the
"shared primary key" style of `@OneToOne` mentioned in
[onetoone.md](onetoone.md) - there, `@MapsId` (with no argument) means
"my whole id *is* the associated entity's id".
