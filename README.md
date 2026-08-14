# Spring Data JPA Relationship Annotation Demo

A small, executable Spring Boot project for learning the most important JPA and Spring Data JPA annotations. It starts with an in-memory H2 database, so it needs no Docker, PostgreSQL, or credentials.

## What this project demonstrates

- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, and `@Column`
- Bidirectional `@OneToOne`: `Student` → `StudentProfile`
- Bidirectional `@OneToMany` / `@ManyToOne`: `Department` → `Course`
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
2. [One-to-many / many-to-one](docs/onetomany.md)
3. [Many-to-many](docs/manytomany.md)
4. [Complete annotation reference](docs/annotations.md)

The entity source files contain comments beside each mapping. The tests in `src/test` are executable examples proving what each mapping does.
