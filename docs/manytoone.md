# `@ManyToOne`: many courses, one department

## In simple terms

This is a relationship from the **many side**. In this demo, many courses can belong to one department:

For example, both “Mechanics” and “Optics” can point to the single “Physics” department. Each individual course points to only one department.

## Small practical example

```java
Department physics = departmentRepository.save(new Department("Physics"));
Course mechanics = new Course("Mechanics");
mechanics.setDepartment(physics);
courseRepository.save(mechanics);
```

This is the most direct way to save the relationship because `Course` owns the foreign key.

## Mapping, line by line

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

`@ManyToOne` says several `Course` objects can refer to one `Department`. `@JoinColumn` names the column that stores the link. Think of the `courses` table as a spreadsheet. Every course row has one `department_id` cell. Several rows can contain the same department id. That is exactly what “many courses to one department” means.

`Course` is the **owning side** because it contains the foreign key (`department_id`). A *foreign key* is a database column that points to a row in another table. Setting `course.setDepartment(department)` is the operation that writes this column. The `Department.courses` collection is useful in Java, but it does not own or write the column.

## Why these options are used

`optional = false` means a course must have a department in Java. `nullable = false` makes the same rule in the database. Using both prevents invalid courses at two layers. `fetch = LAZY` means JPA starts by loading only the course; it gets the department only when your code asks for it. This avoids loading extra data you may not need.

The related [one-to-many guide](onetomany.md) explains the parent-side collection. These two annotations describe the same database relationship from opposite directions.

## Variations, mistakes, and production advice

- If a course may temporarily have no department, omit `optional = false` and allow a nullable column. Only do this when it is a real business rule.
- `@ManyToOne` is EAGER by the JPA default. EAGER often loads more data than expected, so this demo explicitly uses LAZY.
- Do not put `CascadeType.REMOVE` from `Course` to `Department`: deleting one course must not delete its shared department.
- A many-to-one is often all you need. Add the `@OneToMany` collection only when your application genuinely needs navigation from parent to children.

## Interview Answer

**Why is `@ManyToOne` usually the owning side?** The many-side table contains the foreign-key column. Changing the `@ManyToOne` field changes that column, so JPA treats it as the owner of the relationship.
