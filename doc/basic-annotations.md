# The Basics: `@Entity`, `@Table`, `@Id`, `@Column`, and friends

Before relationships make sense, you need the annotations that turn a plain
Java class into something JPA can store as a database row.

## `@Entity`

```java
@Entity
public class Department { ... }
```

Marks a class as a JPA-managed persistent type. Requirements:

- A no-arg constructor (can be `protected`, as used throughout this project).
- Not `final`, and no `final` fields that JPA needs to set (Hibernate needs
  to be able to create/modify instances via reflection).
- Exactly one `@Id` field (or an `@EmbeddedId` - see [embeddable.md](embeddable.md)).

Without `@Table`, the table name defaults to the entity's class name (some
providers lower-case/snake_case it via a "physical naming strategy" - the
Spring Boot default does).

## `@Table`

```java
@Table(name = "departments")
```

Optional. Overrides the default table name. Also lets you declare
`uniqueConstraints`, `indexes`, etc. We use it throughout this project simply
to get predictable, readable table names (`departments`, `employees`, ...).

## `@Id` and `@GeneratedValue`

Every entity needs exactly one primary key field, marked `@Id`. See
[keys-and-generation.md](keys-and-generation.md) for the full breakdown of
`@GeneratedValue` strategies (`IDENTITY`, `SEQUENCE`, `AUTO`, `TABLE`, `UUID`).

## `@Column`

```java
@Column(nullable = false, unique = true)
private String name;
```

Optional - without it, a column is still created (named after the field,
nullable, no constraints). Use it to customize:

- `name` - override the column name.
- `nullable` - adds a `NOT NULL` constraint.
- `unique` - adds a `UNIQUE` constraint.
- `length` - for `String` columns (default 255).
- `columnDefinition` - escape hatch for raw DDL when you need it.

See it in action on `User.username` ( [`User.java`](../src/main/java/com/example/jparelationships/onetoone/User.java) )
and `Course.title` ( [`Course.java`](../src/main/java/com/example/jparelationships/manytomany/Course.java) ).

## `@Transient` (not used in this project, but good to know)

```java
@Transient
private int cachedAgeInDays;
```

Marks a field that JPA should **completely ignore** - no column, never
read or written. Useful for derived/computed values you still want as a
regular Java field. (A plain `static` field, or a field with no getter used
anywhere persistence-related, is ignored too, but `@Transient` makes the
intent explicit and works even for otherwise-persistable field types.)

## Bean Validation annotations (`@NotBlank`, etc.)

You'll notice `@NotBlank` on `User.username`. These are **not** JPA
annotations - they're Jakarta Bean Validation annotations that Spring
(via `spring-boot-starter-validation`) can enforce on incoming
`@RequestBody` objects, and that Hibernate *also* happens to honor at
flush-time by default (`hibernate.validator.apply_to_ddl` can even turn
`@NotNull` into a DDL `NOT NULL` constraint). They're included here just to
show the two annotation families coexist cleanly on the same fields.

## Lombok? Not used here, on purpose

Many real projects use Lombok (`@Data`, `@Getter`, etc.) to eliminate
getter/setter/`toString`/`equals` boilerplate. This project writes them out
by hand instead, because:

1. Seeing every method makes it obvious *which* fields are mutable and how
   both sides of a bidirectional relationship are kept in sync
   (see `assignProfile`, `addEmployee`, `enrollIn` throughout the codebase).
2. Lombok's `@Data` generates `equals()`/`hashCode()`/`toString()` from
   **every** field by default, including relationship fields. On a
   bidirectional association that recurses straight into infinite loops /
   `StackOverflowError`. It's a well-known trap - avoiding Lombok here keeps
   the focus on the JPA concepts instead of Lombok configuration.
