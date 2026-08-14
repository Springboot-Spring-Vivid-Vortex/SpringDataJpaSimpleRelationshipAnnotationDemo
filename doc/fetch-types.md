# `FetchType.LAZY` vs `FetchType.EAGER`

Fetching controls **when** an association is loaded from the database.
It is entirely independent of cascading (which controls what happens on
*write* - see [cascade-types.md](cascade-types.md)).

## The defaults nobody remembers (and why this project overrides them everywhere)

| Annotation | Default fetch |
|---|---|
| `@OneToOne` | `EAGER` |
| `@ManyToOne` | `EAGER` |
| `@OneToMany` | `LAZY` |
| `@ManyToMany` | `LAZY` |

The *-to-one* annotations default to `EAGER`; the *-to-many* ones default
to `LAZY`. This is one of the most commonly-misremembered facts in JPA -
most people assume everything is lazy by default. Every relationship in
this project sets `fetch` **explicitly** specifically so you never have to
guess or look it up.

- `Employee.department` (`@ManyToOne`) is set to `LAZY` - explicitly
  overriding the `EAGER` default, because loading an employee should not
  silently also load its department every single time.
- `User.profile` (`@OneToOne`) is left `EAGER` - loading a user without its
  profile is rarely useful here, so the default is kept and simply spelled
  out for clarity.
- `Department.employees` (`@OneToMany`) and `Student.courses`/
  `Course.students` (`@ManyToMany`) are `LAZY` (the default, spelled out).

## `LAZY`: loaded on first access, inside an open session

```java
Department dept = departmentRepository.findById(1L).orElseThrow();
dept.getEmployees();       // <-- SQL for employees fires HERE, not above
```

This only works while a Hibernate session (JPA persistence context) is
still open - typically, inside a `@Transactional` method. If you return the
same `Department` out of a non-transactional method and try to access
`getEmployees()` later (e.g. while Jackson serializes it to JSON *after*
the controller method has returned), you get:

```
org.hibernate.LazyInitializationException:
failed to lazily initialize a collection: could not initialize proxy - no Session
```

### How this project avoids it

Two different techniques, both shown in the code:

1. **`JOIN FETCH` in the repository query** -
   [`DepartmentRepository.findByIdWithEmployees`](../src/main/java/com/example/jparelationships/onetomany/DepartmentRepository.java)
   loads the department *and* its employees in one SQL query with a JOIN,
   so the collection is already populated - no lazy loading needed at all.
2. **Touch it inside `@Transactional`** -
   [`DepartmentController.findAll`](../src/main/java/com/example/jparelationships/onetomany/DepartmentController.java)
   and [`StudentController`](../src/main/java/com/example/jparelationships/manytomany/StudentController.java)
   are annotated `@Transactional(readOnly = true)` and call
   `.getEmployees().size()` / `.getCourses().size()` before returning, so
   the lazy collection is fully loaded while the session is still open,
   *before* Jackson ever touches it.

## `EAGER`: loaded immediately, every time, whether you need it or not

```java
User user = userRepository.findById(1L).orElseThrow();
// user.getProfile() is already populated - Hibernate joined it in
// automatically as part of the very query that loaded `user`.
```

Simple and safe against `LazyInitializationException`, but easy to abuse:
mark enough associations `EAGER` and a single `findById` call can end up
loading a huge, unwanted chunk of your object graph on every request. This
is why `@ManyToOne`/`@OneToOne` (which default to `EAGER`) are the ones
worth double-checking on every entity you write.

## The N+1 selects problem

```java
List<Department> departments = departmentRepository.findAll(); // 1 query
departments.forEach(d -> d.getEmployees().size());              // N more queries, one per department!
```

With `LAZY` fetching and no `JOIN FETCH`, accessing a collection on each of
`N` parent rows fires `N` additional `SELECT` statements - the classic
"N+1 selects" performance problem. `DepartmentRepository.findByIdWithEmployees`
shows the standard fix (`JOIN FETCH` in JPQL); for lists of many parents,
`@BatchSize` (loads lazy collections in batches instead of one at a time)
is another common mitigation not shown in this project to keep things
focused, but worth knowing exists.

## Rule of thumb

Default to `LAZY` everywhere (explicitly, on every association, exactly
like this project does), and reach for `JOIN FETCH` / entity graphs on the
*specific* query where you actually need the association loaded. Almost
never leave `@ManyToOne`/`@OneToOne` as implicit `EAGER` by omission - make
the choice explicit so the next reader (including future you) doesn't have
to memorize the default table above.
