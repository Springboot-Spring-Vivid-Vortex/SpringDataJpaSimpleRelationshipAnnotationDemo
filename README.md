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

## How to learn from this repository

For each guide, use this loop:

1. Read the simple explanation and run the smallest example mentally.
2. Open the matching entity class and find the same annotation.
3. Read the matching test. It proves what is actually stored in H2.
4. Only then move to cascade, lazy loading, ownership, and production trade-offs.

This order matters. In JPA, unfamiliar terms can make a small foreign-key idea look much more complicated than it is.

## Important production note

H2 and `create-drop` are deliberately used to keep learning easy: every run starts with an empty database. In production, use your real database (often PostgreSQL) and manage schema changes with Flyway or Liquibase. Do not expose these bidirectional entities directly from a REST controller; use DTOs to avoid recursive JSON and accidental lazy-loading queries.

## Interview Answer

**What is Spring Data JPA?** It is a Spring module built on JPA that reduces database boilerplate by creating repository implementations for entity classes. JPA maps Java objects and their relationships to tables; Spring Data JPA provides convenient CRUD, paging, sorting, and query support.

**Common follow-up:** *Does Spring Data JPA replace Hibernate?* No. Spring Data JPA is a repository abstraction; Hibernate is commonly the JPA provider that performs the actual object-relational mapping and SQL work.

**Common trap:** `save()` is not a guarantee of an immediate SQL `INSERT`. JPA may delay SQL until flush or transaction commit. The tests use `saveAndFlush()` where immediate database verification matters.
