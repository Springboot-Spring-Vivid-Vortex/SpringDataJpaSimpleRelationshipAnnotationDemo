# `@OneToMany` and `@ManyToOne`: one department, many courses

A course has one department. A department has many courses. The database needs only one foreign key: `courses.department_id`.

```java
// Course: owning side because it stores department_id
@ManyToOne(fetch = FetchType.LAZY)
private Department department;

// Department: inverse side
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Course> courses = new ArrayList<>();
```

The many-to-one side nearly always owns this relation, because it holds the foreign key. `mappedBy = "department"` refers to the Java field name in `Course`, not a database column.

Always keep both Java objects in sync. The helper `department.addCourse(course)` adds to the list and calls `course.setDepartment(department)`. Calling only `getCourses().add(course)` does not set the foreign key.

By JPA default, `@ManyToOne` is `EAGER`, which can unexpectedly load departments. This demo uses `LAZY` explicitly. `@OneToMany` is `LAZY` by default. `orphanRemoval = true` makes `removeCourse` delete the course row; omit it if a course should simply move to another parent or continue to exist.
