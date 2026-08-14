# Why Bidirectional Relationships Break JSON, and How This Project Fixes It

This isn't a JPA annotation topic, but it's the very next problem everyone
hits the moment they put bidirectional entities behind a `@RestController`
- worth documenting right next to the relationships that cause it.

## The problem

```java
class Department {
    List<Employee> employees; // has a `department` field pointing back
}
class Employee {
    Department department;    // has an `employees` field pointing back
}
```

Serialize a `Department` to JSON with a naive Jackson setup and it tries
to write: `department -> employees -> [ employee -> department -> employees -> ... ]`
forever, until it overflows the stack (`StackOverflowError`) or Jackson
detects the cycle and throws `JsonMappingException: Infinite recursion`.

Every bidirectional relationship in this project has exactly this shape,
so every one of them needs an explicit fix.

## Fix 1: `@JsonManagedReference` / `@JsonBackReference` (used for `@OneToMany`/`@ManyToOne` and `@OneToOne`)

```java
// Department.employees (the "forward", serialized side)
@JsonManagedReference
private List<Employee> employees;

// Employee.department (the "back", skipped side)
@JsonBackReference
private Department department;
```

Jackson serializes the `@JsonManagedReference` side normally, but
completely **omits** the `@JsonBackReference` field when writing JSON (and
correctly re-links it when *reading* JSON back into objects). Result:
`GET /api/departments` returns each department with its full list of
employees, and each employee in that list simply has no `department` key
- avoiding the cycle. The same pair is used for `User.profile`
(`@JsonManagedReference`) / `UserProfile.user` (`@JsonBackReference`).

## Fix 2: `@JsonIgnoreProperties` (used for `@ManyToMany`)

`@JsonManagedReference`/`@JsonBackReference` assumes a clear "parent" and
"child" - awkward for `@ManyToMany`, where both sides are symmetric peers.
Instead:

```java
// Student.courses
@JsonIgnoreProperties("students")
private Set<Course> courses;

// Course.students
@JsonIgnoreProperties("courses")
private Set<Student> students;
```

This says "when serializing a `Course` reached through `Student.courses`,
skip *that course's* `students` field" (and symmetrically the other way).
Both sides remain independently serializable at the top level
(`GET /api/students` shows each student's courses; the same course's
`students` field just isn't repeated inside that nested view) - the cycle
is broken by omitting one specific field one level down, rather than
hiding an entire field everywhere.

## Why not just `@JsonIgnore`?

`@JsonIgnore` on, say, `Employee.department` would work to break the cycle
too, but it hides the field **unconditionally**, including from
`GET /api/departments/{id}` where you might actually want to confirm which
department an employee (reached some other way) belongs to.
`@JsonManagedReference`/`@JsonBackReference` and `@JsonIgnoreProperties`
are more surgical: they break only the specific cyclical path, not the
field in general.

## This is a Jackson/HTTP-layer concern, not a JPA one

None of these annotations affect persistence at all - Hibernate never
looks at them. They matter purely because this project also exposes the
JPA entities directly as REST responses for convenience. Real-world APIs
often introduce separate DTO ("Data Transfer Object") classes specifically
to avoid ever serializing `@Entity` objects directly - sidestepping this
whole problem, at the cost of extra mapping code. Both approaches are
valid; this project uses direct entity serialization (with the fixes
above) to keep the example code minimal and focused on JPA itself.
