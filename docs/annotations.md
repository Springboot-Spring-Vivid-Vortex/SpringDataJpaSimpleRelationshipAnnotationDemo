# Spring Data JPA annotation reference

Spring Data JPA builds on **Jakarta Persistence (JPA)** annotations. JPA describes object-to-table mapping; Spring Data supplies repository interfaces and query conveniences. Import JPA annotations from `jakarta.persistence`, not the older `javax.persistence` package.

## Entity and column mapping

| Annotation | What it does | Important detail |
| --- | --- | --- |
| `@Entity` | Marks a class as a persistent table-backed type. | Needs an identifier and a no-argument constructor (it may be `protected`). |
| `@Table(name = "...")` | Chooses table name, schema, indexes, unique constraints. | Optional; naming defaults apply without it. |
| `@Id` | Marks the primary-key field/property. | Every entity needs exactly one identifier strategy. |
| `@GeneratedValue` | Lets the provider generate an id. | `IDENTITY` uses an auto-increment column; `SEQUENCE` is often best for PostgreSQL; `AUTO` delegates selection. Do not assign generated ids yourself. |
| `@Column` | Configures column name, length, nullable, unique, precision and scale. | `nullable` helps schema generation; database constraints are the real enforcement. |
| `@Transient` | Excludes a field from persistence. | Different from Java `transient`; use this for calculated values. |
| `@Lob` | Stores long text or binary content. | Maps `String` to CLOB-like and `byte[]` to BLOB-like storage. |
| `@Enumerated(EnumType.STRING)` | Stores an enum name. | Prefer `STRING`; ordinal storage breaks when enum order changes. |
| `@Convert` / `@Converter` | Converts a Java type to a database type. | Useful for value objects; converters should be deterministic. |
| `@Version` | Enables optimistic locking. | Update conflict throws an exception instead of silently overwriting newer data. |

JPA can use field access (put `@Id` on a field, as this demo does) or property access (put it on a getter). Do not mix the two styles within one entity.

## Relationships

Before the reference table, read [relationship terms in plain English](relationships.md). In particular, “owning side” does **not** mean “the more important business object”; it only means the side JPA uses to save the link.

| Annotation | Database shape | Owner and common choice |
| --- | --- | --- |
| `@OneToOne` | Foreign key with a unique constraint. | The side with `@JoinColumn` owns it. |
| `@ManyToOne` | Foreign key on the many-side table. | It is normally the owner. Explicitly choose `LAZY`; JPA defaults it to eager. |
| `@OneToMany(mappedBy = "...")` | Reverse view of a many-to-one. | `mappedBy` marks the inverse side; no extra join table is created. |
| `@ManyToMany` | A join table. | One side owns it; avoid remove cascades for shared data. |
| `@JoinColumn` | Names/configures a foreign-key column. | Use `referencedColumnName` only when it is not the target primary key. |
| `@JoinTable` | Names/configures a relationship join table. | Optional for many-to-many; also useful for unusual one-to-one mappings. |
| `@OrderBy` / `@OrderColumn` | Orders a collection. | `@OrderBy` sorts on read; `@OrderColumn` stores list position. |
| `@ElementCollection` | Persists value types, not entities, in a separate table. | Use for values like tags or addresses that have no identity/lifecycle. |

`cascade` controls which operations propagate: `PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`, or `ALL`. It is about lifecycle operations, not SQL query loading. `orphanRemoval = true` deletes a child removed from a parent collection/reference; it is not appropriate for shared entities. Collection relationships default to `LAZY`; to-one relationships default to `EAGER`. Prefer explicit `LAZY`, then fetch deliberately with a fetch join or entity graph.

## Inheritance and reusable state

| Annotation | Use |
| --- | --- |
| `@MappedSuperclass` | Shares mapped fields (for example id and audit times) with subclasses; it has no table of its own. |
| `@Inheritance` | Persists an entity hierarchy. `SINGLE_TABLE` is simple but sparse; `JOINED` is normalized but joins; `TABLE_PER_CLASS` is uncommon. |
| `@DiscriminatorColumn` / `@DiscriminatorValue` | Identifies subclasses in `SINGLE_TABLE` inheritance. |
| `@Embeddable` and `@Embedded` | Stores a small value object’s fields in its owner table. Use `@AttributeOverride` to rename reused columns. |

## Spring Data repository annotations

| Annotation | Use |
| --- | --- |
| `@Repository` | Stereotype and exception translation. | Spring Data creates repositories automatically, so normally you do **not** add it to a `JpaRepository` interface. |
| `@Query` | Defines JPQL or native SQL on a repository method. | JPQL uses entity and field names, not table/column names. Set `nativeQuery = true` only for database SQL. |
| `@Modifying` | Marks an `@Query` as update/delete. | Run it inside a transaction; consider `clearAutomatically = true` after bulk updates. |
| `@EntityGraph` | Fetches named attributes in one query. | A focused alternative to making relationships eager. |
| `@Lock` | Sets a JPA lock mode on a repository query. | Use only with a transaction and a clear concurrency reason. |
| `@Procedure` | Calls a stored procedure. | Keep database-specific procedures isolated. |
| `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` | Auditing fields. | Need `@EnableJpaAuditing` and `AuditingEntityListener`. |

`JpaRepository<Student, Long>` in this demo already supplies `save`, `findById`, `findAll`, paging, sorting, deletion, flushing, and batch helpers. Prefer derived methods such as `findByName(String name)` for simple predicates. Use `@Query` when the method name becomes unclear.

## Safe habits

1. Put database truth (foreign keys, unique constraints, not-null constraints) in schema mappings or migrations.
2. Change an association through helper methods so both Java sides agree.
3. Avoid `CascadeType.ALL` for shared associations, especially many-to-many.
4. Do not return entities directly from a REST API in real applications: bidirectional links can recurse during JSON serialization. Use DTOs.
5. Avoid `FetchType.EAGER` as a “fix”; it often causes unnecessary queries. Use transactions, fetch joins, projections, or `@EntityGraph`.
6. For production schema changes, use Flyway or Liquibase rather than `ddl-auto=create-drop`.
