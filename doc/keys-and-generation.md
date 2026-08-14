# Primary Keys: `@Id` and `@GeneratedValue`

Every entity in this project uses:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

## `GenerationType.IDENTITY`

Delegates key generation entirely to the database's native
auto-increment column (`IDENTITY` in H2, `AUTO_INCREMENT` in MySQL,
`GENERATED ALWAYS AS IDENTITY` in Postgres). Simple and universally
supported - the choice for this project.

**Trade-off:** Hibernate cannot know the id until *after* the `INSERT`
actually runs, which disables JDBC batch-inserting of new entities. For a
teaching project this doesn't matter; for a high-throughput production
system inserting thousands of rows, `SEQUENCE` is usually preferred.

## `GenerationType.SEQUENCE`

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dept_seq")
@SequenceGenerator(name = "dept_seq", sequenceName = "department_seq", allocationSize = 50)
private Long id;
```

Uses a database sequence object. Hibernate can pre-fetch a block of ids
(`allocationSize`) and assign them in memory *before* inserting, which
allows batching. Supported by Postgres, Oracle, H2 - **not** by MySQL
(before MySQL 8) or SQL Server in the traditional sense. The best choice
when your database supports it and you care about insert performance.

## `GenerationType.AUTO`

Lets Hibernate pick a strategy based on the configured database dialect
(often resolves to `SEQUENCE` or `IDENTITY`). Convenient for
database-agnostic code, but slightly less predictable/explicit - this
project prefers being explicit with `IDENTITY`.

## `GenerationType.TABLE`

Simulates a sequence using an ordinary database table with a counter row,
updated (with locking) on every id request. Works on literally any
database but is the slowest option due to the extra table access +
locking on every insert. Rarely used today outside of databases that
support neither native identity columns nor sequences.

## `GenerationType.UUID` (Hibernate 6+ / Jakarta Persistence 3.1+)

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

Generates a random UUID in application memory - no round-trip to the
database needed to obtain an id, and ids are globally unique across
tables/databases/services (handy for distributed systems, offline-first
apps, or merging data from multiple sources). Trade-off: UUIDs are 16
bytes vs. 8 for a `Long`, and being random (not sequential) can hurt
clustered-index insert performance/fragmentation on some databases.

## Composite keys: `@EmbeddedId` / `@IdClass`

Not every entity has a single-column key. See
[embeddable.md](embeddable.md) for `@EmbeddedId`, used by
`CourseEnrollment` to model a primary key made of two foreign keys
(`student_id` + `course_id`).

`@IdClass` is JPA's other option for composite keys - functionally similar
to `@EmbeddedId` but the key fields are duplicated directly onto the entity
(instead of grouped in an embeddable object) and mirrored in a separate,
annotation-free "id class". `@EmbeddedId` is generally preferred today
because the key fields live in exactly one place.
