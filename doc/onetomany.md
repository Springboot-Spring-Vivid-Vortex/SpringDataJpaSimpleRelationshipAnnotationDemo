# `@OneToMany` and `@ManyToOne`

**Example:** [`Department`](../src/main/java/com/example/jparelationships/onetomany/Department.java) (one) &lt;-&gt; [`Employee`](../src/main/java/com/example/jparelationships/onetomany/Employee.java) (many)

These two annotations are always two views of the *same* relationship, so
they're explained together. One `Department` has many `Employee`s; each
`Employee` belongs to exactly one `Department`.

## The schema this produces

```
departments                 employees
+----+-------------+        +----+---------------+---------------+
| id | name        |        | id | name          | department_id |
+----+-------------+        +----+---------------+---------------+
| 1  | Engineering |        | 1  | Grace Hopper  | 1             |
+----+-------------+        | 2  | Linus Torvalds| 1             |
                             +----+---------------+---------------+
```

Just **one** extra column, `employees.department_id` - no join table.

## The owning side: `@ManyToOne` (`Employee.department`)

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

**This is the owning side.** `@JoinColumn` here is what actually creates
the `department_id` foreign key column. Whenever you set/change
`employee.department`, that's the field Hibernate looks at to decide the
`UPDATE`/`INSERT` for the foreign key.

`fetch = FetchType.LAZY` overrides the `@ManyToOne` **default of EAGER** -
see the callout in [fetch-types.md](fetch-types.md). This is arguably the
single most impactful line to remember from this whole project: forgetting
to set `@ManyToOne`/`@OneToOne` to `LAZY` is the #1 cause of accidentally
loading half your object graph on every single query.

## The inverse side: `@OneToMany` (`Department.employees`)

```java
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Employee> employees = new ArrayList<>();
```

`mappedBy = "department"` says "look at the `department` field on
`Employee` for the foreign key - I don't own one". A bare `@OneToMany`
(without `mappedBy`) is always the *inverse* side of *something*; there is
no such thing as an owning `@OneToMany` when a matching `@ManyToOne`
exists on the other entity.

## ⚠️ The unidirectional `@OneToMany` pitfall

If you delete `Employee.department` entirely and keep only:

```java
@Entity
class Department {
    @OneToMany
    @JoinColumn(name = "department_id") // without this, Hibernate defaults to a join table!
    private List<Employee> employees;
}
```

...you get a **unidirectional** `@OneToMany`. Without an explicit
`@JoinColumn`, Hibernate's default behavior is to create a **separate join
table** (e.g. `department_employees` with `department_id`/`employees_id`
columns) even though `employees` already has a perfectly good place for a
foreign key. That's an extra table and an extra join for every query, and
it surprises almost everyone the first time they see the generated SQL.
The fix, if you don't need the `@ManyToOne` side in Java: add
`@JoinColumn(name = "department_id")` directly on the `@OneToMany`, which
tells Hibernate to reuse the child table's column instead of creating a
join table. But in general: **prefer the bidirectional
`@ManyToOne`(owning)/`@OneToMany`(mappedBy) pattern used in this project**
whenever a foreign key column naturally belongs on the "many" table, which
is nearly always.

## Keeping both sides in sync

```java
public void addEmployee(Employee employee) {
    employees.add(employee);
    employee.setDepartmentInternal(this);
}
```

Same reasoning as `User.assignProfile` in [onetoone.md](onetoone.md):
JPA never does this for you, so `Department` exposes `addEmployee`/
`removeEmployee` instead of a mutable public list, guaranteeing the two
sides can never drift apart in memory.

## `cascade` and `orphanRemoval`

Covered in depth in [cascade-types.md](cascade-types.md). Short version,
as used here: `CascadeType.ALL` means saving/deleting a `Department` also
saves/deletes its `Employee`s. `orphanRemoval = true` means calling
`department.removeEmployee(e)` and saving deletes that employee's row too,
even though nobody called `employeeRepository.delete(e)` directly.

## Also demonstrated on `Employee`

- **`@Embedded` / `@Embeddable`** (`Employee.homeAddress`) - see
  [embeddable.md](embeddable.md).
- **`@ElementCollection`** (`Employee.skills`) - see
  [elementcollection.md](elementcollection.md).

Both are unrelated to `@OneToMany`/`@ManyToOne` conceptually but live on
`Employee` here to keep the number of packages/tables small.
