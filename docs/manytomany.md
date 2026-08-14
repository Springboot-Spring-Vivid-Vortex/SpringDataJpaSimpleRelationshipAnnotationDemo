# `@ManyToMany`: students enroll in courses

Relational databases represent many-to-many with a third table. Hibernate creates a join table with a student id and a course id.

```java
// Student: owning side
@ManyToMany
private Set<Course> courses = new LinkedHashSet<>();

// Course: inverse side
@ManyToMany(mappedBy = "courses")
private Set<Student> students = new LinkedHashSet<>();
```

Only one side is the owner. Here it is `Student`, because it has no `mappedBy`. Calling `student.enrollIn(course)` updates both in-memory sets, but only the owning set controls inserts/deletes in the join table.

Do not use `CascadeType.REMOVE` (or usually `ALL`) on a many-to-many: deleting a student must not delete shared courses. A `Set` prevents duplicate links in memory; add a unique constraint to the join table if duplicates must also be prohibited at database level.

When the join table needs data such as `enrolledAt`, `grade`, or `role`, do **not** use `@ManyToMany`. Create an `Enrollment` entity with two `@ManyToOne` fields instead.
