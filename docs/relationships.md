# Relationship terms in plain English

## Start with one small example

Suppose the `courses` table has this row:

| id | title | department_id |
| --- | --- | --- |
| 7 | Databases | 2 |

`department_id = 2` means course 7 belongs to the department whose id is 2. JPA turns that cell into `course.getDepartment()` in Java. This one idea explains most relationship annotations.

| Term | Simple meaning |
| --- | --- |
| Entity | A Java class JPA saves as rows in a database table. |
| Primary key | The unique id of a row, such as `course.id`. |
| Foreign key | A column that points to another table’s primary key, such as `courses.department_id`. |
| Owning side | The Java field whose changes JPA uses to write the relationship to the database. It normally has `@JoinColumn` or `@JoinTable`. |
| Inverse side | The matching Java field that reads the relationship. It has `mappedBy` and does not create a second database link. |
| `mappedBy` | The Java field name of the owning side. It means “the other field already stores this relationship.” |
| Join table | A small middle table that stores links between two tables, such as `student_course_enrollments`. |
| Cascade | An instruction to apply an operation, such as save or delete, to a related object too. |
| Orphan | A child object no longer attached to its parent. `orphanRemoval = true` deletes it. |
| Lazy loading | Do not retrieve the related object until code needs it. |

## Which mapping should I choose?

| Real-world sentence | Mapping | Database design |
| --- | --- | --- |
| “A student has one profile.” | `@OneToOne` | One foreign key with a unique constraint. |
| “A department has many courses.” | `@OneToMany` and `@ManyToOne` | `courses.department_id`. These are two Java views of one foreign key. |
| “Students can take many courses; courses can have many students.” | `@ManyToMany` | A join table with `student_id` and `course_id`. |

## The important rule

For a bidirectional relationship, update **both** Java fields so the objects you hold in memory agree. Then make sure the owning side is updated, because only that side writes the database relationship. The helper methods in this project (`addCourse` and `enrollIn`) do both jobs.

## Production trade-offs

Bidirectional mappings are convenient but add responsibility: JSON serialization can loop from student → courses → students forever, and lazy collections may run unexpected SQL. In REST applications, DTOs are usually safer than returning entities directly. Start with the smallest navigation direction your application needs; add the reverse collection only when it is useful.

## Common interview questions and traps

| Question | Short answer | Trap to avoid |
| --- | --- | --- |
| What is a foreign key? | A column that refers to a row in another table. | It is a database concept; a Java reference alone does not enforce it. |
| What does `mappedBy` do? | It marks the inverse side and names the owning Java field. | It is not a column name and does not create a new mapping. |
| Do both sides save a bidirectional relationship? | No; the owning side writes the link. | Still update both Java objects so in-memory state is correct. |
| Is bidirectional always better? | No; it is useful only when navigation is needed both ways. | Extra navigation can create N+1 queries and JSON recursion. |

## Interview Answer

**What is the difference between the owning and inverse side?** The owning side writes the foreign key or join-table row. The inverse side uses `mappedBy` to describe the same link without creating another database relationship. For a bidirectional association, application code should update both object fields.
