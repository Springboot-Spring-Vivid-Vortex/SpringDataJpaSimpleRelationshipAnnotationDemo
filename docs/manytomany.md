# `@ManyToMany`: students enroll in courses

Relational databases represent many-to-many with a third table. This demo explicitly names it `student_course_enrollments`; each row contains a `student_id` and `course_id`.

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

Only one side is the owner. Here it is `Student`, because it has `@JoinTable` and no `mappedBy`. Calling `student.enrollIn(course)` updates both in-memory sets, but only the owning set controls inserts/deletes in the join table. `joinColumns` names the column for this entity (`Student`); `inverseJoinColumns` names the column for the other entity (`Course`).

Do not use `CascadeType.REMOVE` (or usually `ALL`) on a many-to-many: deleting a student must not delete shared courses. A `Set` prevents duplicate links in memory; add a unique constraint to the join table if duplicates must also be prohibited at database level.

When the join table needs data such as `enrolledAt`, `grade`, or `role`, do **not** use `@ManyToMany`. Create an `Enrollment` entity with two `@ManyToOne` fields instead.
