# Spring Data JPA Relationship Annotation Demo

A small, executable Spring Boot project for learning the most important JPA and Spring Data JPA annotations. It starts with an in-memory H2 database, so it needs no Docker, PostgreSQL, or credentials.

## Start here: what is JPA?

JPA is a Java standard that lets you represent database tables as normal Java classes. Spring Data JPA builds on it and gives you ready-made repository methods such as `save` and `findById`.

Small example: `Student` is a Java object. After `studentRepository.save(student)`, JPA stores it as a row in the `students` table. Relationship annotations tell JPA how a `Student` row connects to rows such as `student_profiles` and `courses`.

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

## Important production note

H2 and `create-drop` are deliberately used to keep learning easy: every run starts with an empty database. In production, use your real database (often PostgreSQL) and manage schema changes with Flyway or Liquibase. Do not expose these bidirectional entities directly from a REST controller; use DTOs to avoid recursive JSON and accidental lazy-loading queries.

## Interview Answer

**What is Spring Data JPA?** It is a Spring module built on JPA that reduces database boilerplate by creating repository implementations for entity classes. JPA maps Java objects and their relationships to tables; Spring Data JPA provides convenient CRUD, paging, sorting, and query support.
