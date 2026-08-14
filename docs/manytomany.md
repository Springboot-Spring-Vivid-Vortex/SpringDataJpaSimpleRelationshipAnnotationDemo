# `@ManyToMany`: students enroll in courses

## In simple terms

Relational databases represent many-to-many with a third table. This demo explicitly names it `student_course_enrollments`; each row contains a `student_id` and `course_id`.

One student can take several courses, and one course can have several students. Neither table can hold a single foreign key for that, so the middle table stores each enrollment link.

## Small practical example

```java
Student ravi = new Student("Ravi");
Course springData = existingCourse;
ravi.enrollIn(springData);
studentRepository.save(ravi);
```

This creates one row in `student_course_enrollments`; it does not duplicate Ravi or the course.

## Mapping, line by line

```java
// Student: owning side
@ManyToMany
@JoinTable(
    name = "student_course_enrollments",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id"))
private Set<Course> courses = new LinkedHashSet<>();

// Course: inverse side
@ManyToMany(mappedBy = "courses")
private Set<Student> students = new LinkedHashSet<>();
```

`@ManyToMany` says both sides can have many links. `@JoinTable` names the middle table. Only one side is the owner. Here it is `Student`, because it has `@JoinTable` and no `mappedBy`. Calling `student.enrollIn(course)` updates both in-memory sets, but only the owning set controls inserts/deletes in the join table. `joinColumns` names the column for this entity (`Student`); `inverseJoinColumns` names the column for the other entity (`Course`).

## Why this design is used

A `Set` prevents duplicate links in Java memory. Do not use `CascadeType.REMOVE` (or usually `ALL`) on a many-to-many: deleting a student must not delete shared courses. Add a unique database constraint to the join table if duplicates must also be prohibited at database level.

## When not to use `@ManyToMany`

When the join table needs data such as `enrolledAt`, `grade`, or `role`, do **not** use `@ManyToMany`. Create an `Enrollment` entity with two `@ManyToOne` fields instead. This is the recommended production design for a real enrollment system because an enrollment is then a first-class Java object with its own id, validation, and lifecycle.

## Common mistakes and production advice

- Updating only `course.getStudents().add(student)` changes the inverse side and will not reliably persist the link. Use `student.enrollIn(course)`.
- `List` allows duplicate links unless you manage it carefully; use `Set` for an association where duplicates make no sense.
- Fetching large many-to-many collections can be expensive. Fetch only the fields/pages you need rather than making it EAGER.

## Interview Answer

**How does JPA map many-to-many?** With a join table. One entity owns the link using `@JoinTable`; the other uses `mappedBy`. If the join needs its own attributes, replace `@ManyToMany` with a separate entity containing two `@ManyToOne` mappings.
