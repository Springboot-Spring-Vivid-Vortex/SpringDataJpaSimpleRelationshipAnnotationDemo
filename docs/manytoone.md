# `@ManyToOne`: many courses, one department

This is a relationship from the **many side**. In this demo, many courses can belong to one department:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

Think of the `courses` table as a spreadsheet. Every course row has one `department_id` cell. Several rows can contain the same department id. That is exactly what “many courses to one department” means.

`Course` is the **owning side** because it contains the foreign key (`department_id`). A *foreign key* is a database column that points to a row in another table. Setting `course.setDepartment(department)` is the operation that writes this column. The `Department.courses` collection is useful in Java, but it does not own or write the column.

`optional = false` means a course must have a department in Java. `nullable = false` makes the same rule in the database. `fetch = LAZY` means JPA starts by loading only the course; it gets the department only when your code asks for it. This avoids loading extra data you may not need.

The related [one-to-many guide](onetomany.md) explains the parent-side collection. These two annotations describe the same database relationship from opposite directions.
