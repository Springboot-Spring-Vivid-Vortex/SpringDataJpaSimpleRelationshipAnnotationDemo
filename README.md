# Spring Data JPA Relationship Annotation Demo

A small, executable Spring Boot project for learning the most important JPA and Spring Data JPA annotations. It starts with an in-memory H2 database, so it needs no Docker, PostgreSQL, or credentials.

## What this project demonstrates

- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, and `@Column`
- Bidirectional `@OneToOne`: `Student` → `StudentProfile`
- Separate, explicit `@OneToMany` and `@ManyToOne` examples: `Department` ↔ `Course`
- Bidirectional `@ManyToMany`: `Student` ↔ `Course`
- `@JoinColumn`, `mappedBy`, cascade types, `orphanRemoval`, owners, inverse sides, and lazy loading
- `JpaRepository` and database-backed relationship integration tests

## Run and test

The project includes a Gradle Wrapper. With Java 21 or newer installed:

```powershell
.\gradlew.bat bootRun
.\gradlew.bat test
```

H2 schema and SQL appear in the console. The database is intentionally in-memory and is removed at shutdown.

## Learn in order

Start with the short, plain-language guides in [docs](docs/README.md):

1. [One-to-one](docs/onetone.md)
2. [Relationship vocabulary](docs/relationships.md)
3. [One-to-many](docs/onetomany.md)
4. [Many-to-one](docs/manytoone.md)
5. [Many-to-many](docs/manytomany.md)
6. [Complete annotation reference](docs/annotations.md)

The entity source files contain comments beside each mapping. The tests in `src/test` are executable examples proving what each mapping does.
