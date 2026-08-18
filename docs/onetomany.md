# `@OneToMany`: one department has many courses

## In simple terms

A course has one department. A department has many courses. The database needs only one foreign key: `courses.department_id`.

The `@OneToMany` field is the Java collection you use when you start with a department and want its courses. It is one half of the same relationship described more directly by `@ManyToOne` on `Course`.

## Small practical example

```java
Department computerScience = new Department("Computer Science");
computerScience.addCourse(new Course("Databases"));
departmentRepository.save(computerScience);
```

`addCourse` is important: it updates both Java objects.

## Mapping, line by line

```java
// Course: owning side because it stores department_id
@ManyToOne(fetch = FetchType.LAZY)
private Department department;

// Department: inverse side
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Course> courses = new ArrayList<>();
```

The many-to-one side nearly always owns this relation, because it holds the foreign key. `mappedBy = "department"` refers to the Java field name in `Course`, not a database column.

Always keep both Java objects in sync. The helper `department.addCourse(course)` adds to the list and calls `course.setDepartment(department)`. Calling only `getCourses().add(course)` does not set the foreign key, because the `Course` field owns it.

## Why these options are used

`cascade = CascadeType.ALL` lets a new course be saved when its department is saved. `orphanRemoval = true` makes `removeCourse` delete the course row; omit it if a course should simply move to another parent or continue to exist. `@OneToMany` is lazy by default, so courses are not loaded until `getCourses()` is used.

## Common mistakes and production advice

- Unidirectional `@OneToMany` without `mappedBy` often creates an extra join table. Prefer this bidirectional `@ManyToOne`/`@OneToMany` design when a child naturally belongs to one parent.
- Do not use `orphanRemoval = true` when a course can be reassigned or independently survive.
- Loading many departments and then calling `getCourses()` for each can cause the N+1 query problem. Use a fetch join, `@EntityGraph`, or a DTO query when needed.

## Interview Answer

**Which side owns a one-to-many relationship?** Usually the `@ManyToOne` side owns it because its table stores the foreign key. `@OneToMany(mappedBy = "department")` is the inverse Java collection and does not create or update that key itself.

**Common follow-up:** *Why not always use a unidirectional `@OneToMany`?* It can create an unnecessary join table and makes the natural foreign-key model less direct. A child-to-parent `@ManyToOne` is usually simpler and more efficient.

**Interview trap:** `cascade = ALL` and `orphanRemoval = true` solve different problems. Cascade propagates operations; orphan removal deletes a child detached from its parent collection.
