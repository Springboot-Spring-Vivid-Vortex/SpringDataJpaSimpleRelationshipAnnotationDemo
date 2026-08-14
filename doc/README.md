# Spring Data JPA Relationship Annotations - Documentation

This folder explains every annotation used in this project, in plain
language, with the actual entity code as reference. Read them in this order
if you're new to JPA:

| # | Doc | Covers | Example entities |
|---|-----|--------|-------------------|
| 1 | [basic-annotations.md](basic-annotations.md) | `@Entity`, `@Table`, `@Id`, `@Column`, `@Transient` - the building blocks | all of them |
| 2 | [keys-and-generation.md](keys-and-generation.md) | `@Id`, `@GeneratedValue` strategies | all of them |
| 3 | [onetoone.md](onetoone.md) | `@OneToOne` | `User` / `UserProfile` |
| 4 | [onetomany.md](onetomany.md) | `@OneToMany`, `@ManyToOne`, `mappedBy`, `@JoinColumn` | `Department` / `Employee` |
| 5 | [manytomany.md](manytomany.md) | `@ManyToMany`, `@JoinTable`, extra-columns pattern | `Student` / `Course` / `CourseEnrollment` |
| 6 | [embeddable.md](embeddable.md) | `@Embeddable`, `@Embedded`, `@EmbeddedId`, `@MapsId` | `Address`, `CourseEnrollmentId` |
| 7 | [elementcollection.md](elementcollection.md) | `@ElementCollection`, `@CollectionTable` | `Employee.skills` |
| 8 | [cascade-types.md](cascade-types.md) | Every `CascadeType`, `orphanRemoval` | all of them |
| 9 | [fetch-types.md](fetch-types.md) | `FetchType.LAZY` vs `EAGER`, N+1 selects, `LazyInitializationException` | all of them |
| 10 | [json-serialization.md](json-serialization.md) | Why bidirectional relationships break JSON serialization, and how we fixed it | all of them |

## The single most important idea in this whole project

Every JPA relationship annotation (`@OneToOne`, `@OneToMany`, `@ManyToOne`,
`@ManyToMany`) only describes **how Java objects reference each other**.
It is a *separate*, explicit decision - made with `mappedBy` and
`@JoinColumn`/`@JoinTable` - which side **owns** the relationship, i.e.
which side's table actually has the foreign key column, and which side's
field changes Hibernate actually looks at when deciding what SQL to run.

Get comfortable with "owning side vs. inverse side" first
([onetomany.md](onetomany.md) explains it with a concrete example) - every
other nuance in this project builds on it.

## Running the project

```bash
./gradlew bootRun
```

Then either:

- Watch the console: [`DataLoader`](../src/main/java/com/example/jparelationships/config/DataLoader.java)
  seeds sample data for every relationship and logs a narrated walkthrough,
  right next to the SQL Hibernate generates (`spring.jpa.show-sql=true`).
- Browse the schema/data at http://localhost:8080/h2-console
  (JDBC URL: `jdbc:h2:mem:jparelationshipsdb`, user `sa`, empty password).
- Hit the REST endpoints, e.g. http://localhost:8080/api/departments

Run the tests with:

```bash
./gradlew test
```
