# `@ManyToMany`

**Example:** [`Student`](../src/main/java/com/example/jparelationships/manytomany/Student.java) &lt;-&gt; [`Course`](../src/main/java/com/example/jparelationships/manytomany/Course.java)

A `Student` can enroll in many `Course`s, and a `Course` can have many
`Student`s. Neither side's table can hold a single foreign key column for
this - you need a **join table** in between.

## The schema this produces

```
students          student_course           courses
+----+------+     +------------+-----------+  +----+----------------+
| id | name |     | student_id | course_id |  | id | title          |
+----+------+     +------------+-----------+  +----+----------------+
| 1  | Ada  |     | 1          | 1         |  | 1  | Algorithms 101 |
+----+------+     | 1          | 2         |  | 2  | Cryptography.. |
                   +------------+-----------+  +----+----------------+
```

## The owning side (`Student.courses`)

```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
)
private Set<Course> courses = new HashSet<>();
```

- `@JoinTable` names the join table and both its foreign-key columns.
  Whichever side declares `@JoinTable` (instead of `mappedBy`) is the
  **owning side** - only changes to `student.courses` cause writes to
  `student_course`.
- `joinColumns` = the column(s) pointing back to *this* entity (`Student`).
- `inverseJoinColumns` = the column(s) pointing to the *other* entity
  (`Course`).

## The inverse side (`Course.students`)

```java
@ManyToMany(mappedBy = "courses")
private Set<Student> students = new HashSet<>();
```

Exactly the same idea as `mappedBy` on `@OneToMany` - "don't manage a join
table here, `Student.courses` already does."

## ⚠️ No `cascade` here - both sides must already be saved

Notice neither `Student.courses` nor `Course.students` declares a
`cascade` attribute (unlike `Department.employees` in
[onetomany.md](onetomany.md)). That's deliberate: a `Course` is shared
catalog data, not something that should be silently created just because
one student happened to enroll in it first, and *definitely* not something
that should be deleted just because one enrolled student gets deleted -
other students may still reference the same course row. `CascadeType.REMOVE`
in particular would be actively dangerous on a many-to-many.

The consequence: both the `Student` and the `Course` must already be
persisted (have a database id) **before** you can link them with
`enrollIn`/`courses.add(...)`. Trying to save a `Student` whose `courses`
set contains a brand-new, never-saved `Course` throws Hibernate's
`TransientObjectException` - see how
[`DataLoader`](../src/main/java/com/example/jparelationships/config/DataLoader.java)
saves both `Course`s first, then enrolls students, to avoid it.

## Why `Set`, not `List`?

Hibernate's default way of updating a many-to-many collection is: delete
**all** rows for the owning entity from the join table, then re-insert the
current contents. Using a `Set`:

1. Naturally prevents "enrolled in the same course twice" duplicate rows
   (a `List` would allow it).
2. Matches the *mathematical* relationship being modeled - "the courses a
   student is in" has no meaningful order or duplicates.

## ⚠️ The classic gotcha: fetching two `@ManyToMany`/`@OneToMany` collections at once

Trying to `JOIN FETCH` two collection-valued associations in the same JPQL
query (e.g. a student's courses *and* something else multi-valued) throws
`MultipleBagFetchException` in older Hibernate versions, or produces a
cartesian-product explosion of duplicate rows even when it doesn't throw.
Using `Set` instead of `List` (as this project does) avoids the exception
entirely, but the cartesian-product row inflation is a reason to fetch
collections one at a time, or reach for `@BatchSize`/`@Fetch(SUBSELECT)`,
in real applications. See [fetch-types.md](fetch-types.md) for the
`JOIN FETCH` example used by `Department`/`Employee`.

## Extra columns on the join table: promote it to an entity

**Example:** [`CourseEnrollment`](../src/main/java/com/example/jparelationships/manytomany/CourseEnrollment.java)

A plain `@ManyToMany` join table (`student_course` above) has *only* the
two foreign key columns. The moment you need to record something about the
enrollment itself - a date, a grade, a status - a bare `@ManyToMany` can't
express it, because `@JoinTable` doesn't map to a Java class you can add
fields to.

The fix: stop modeling it as `@ManyToMany` and model the join table itself
as a real `@Entity` instead, with two `@ManyToOne`s replacing the two
`@ManyToMany` sides:

```java
@Entity
@Table(name = "course_enrollments")
public class CourseEnrollment {

    @EmbeddedId
    private CourseEnrollmentId id;      // (student_id, course_id) composite key

    @ManyToOne
    @MapsId("studentId")                // student_id column is BOTH the FK and part of the PK
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDate enrolledOn;       // <-- the extra column a plain join table couldn't have
    private String grade;
}
```

- `@EmbeddedId` + `@MapsId` is explained in full in
  [embeddable.md](embeddable.md). In short: `CourseEnrollmentId` (a
  `studentId`+`courseId` pair) is the primary key, and `@MapsId` tells
  each `@ManyToOne` "your foreign key column *is* one of the primary key's
  fields - don't create a second column for it".
- The resulting table (`course_enrollments`) looks exactly like the
  `student_course` join table above, plus `enrolled_on` and `grade`
  columns.
- Trade-off versus plain `@ManyToMany`: you lose the convenience of
  `student.getCourses()` returning `Course` objects directly - you now
  navigate through `CourseEnrollment.getCourse()` instead. That's the
  standard price of the extra-columns pattern.

In this demo, `Student`/`Course` (plain `@ManyToMany`) and
`CourseEnrollment` (association-entity pattern) both exist side by side
purely so you can compare them - a real application models a given
relationship with **one or the other**, not both.
