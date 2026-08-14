# Spring Data JPA Relationship Annotations - Demo

A small, self-contained Spring Boot project that demonstrates **every
common Spring Data JPA relationship annotation** with simple, heavily
commented, runnable examples - `@OneToOne`, `@OneToMany`, `@ManyToOne`,
`@ManyToMany`, `@Embeddable`/`@Embedded`, `@EmbeddedId`, `@MapsId`, and
`@ElementCollection` - plus the cross-cutting concerns (`cascade`, `fetch`,
JSON serialization of bidirectional entities) that trip people up in
practice.

## 📖 Start with the docs

**[`/doc`](doc/README.md)** explains every annotation used here in plain
language, one concept per file, with the actual entity code as reference.
That's the real content of this repository - the code below exists to make
those docs runnable and testable.

## Quick start

Requires JDK 21 (or newer) on your `PATH`/`JAVA_HOME`. No database
installation needed - the app uses an in-memory H2 database.

```bash
./gradlew bootRun
```

Then:

- Watch the console for a narrated walkthrough of each relationship
  (seeded by [`DataLoader`](src/main/java/com/example/jparelationships/config/DataLoader.java)),
  right next to the SQL Hibernate generates for it.
- Browse the live schema/data at http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:jparelationshipsdb`, user `sa`, empty password).
- Explore the REST endpoints:
  - `GET /api/users` - `@OneToOne`
  - `GET /api/departments`, `GET /api/departments/{id}` - `@OneToMany`/`@ManyToOne`, `@Embedded`, `@ElementCollection`
  - `GET /api/students`, `GET /api/students/courses` - `@ManyToMany`

Run the test suite:

```bash
./gradlew test
```

## Project layout

```
src/main/java/com/example/jparelationships/
├── common/          Address (@Embeddable, shared by the onetomany demo)
├── onetoone/         User <-> UserProfile                  (@OneToOne)
├── onetomany/         Department <-> Employee               (@OneToMany / @ManyToOne, @Embedded, @ElementCollection)
├── manytomany/         Student <-> Course                    (@ManyToMany)
│                       CourseEnrollment                      (@EmbeddedId / @MapsId - many-to-many with extra columns)
└── config/           DataLoader - seeds sample data & narrates each relationship on startup

doc/                  One Markdown file per concept - see doc/README.md
```

Every entity, repository, and controller is commented in-place explaining
*what* each annotation does and *why* it's configured that way - the
Markdown docs go deeper into the concepts and nuances behind them.
