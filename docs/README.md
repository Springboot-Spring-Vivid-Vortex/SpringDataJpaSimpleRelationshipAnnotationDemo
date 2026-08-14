# Spring Data JPA learning guide

This folder explains the annotations used by the runnable sample. Read the relationship guides first, then use `annotations.md` as a reference.

- [One-to-one](onetone.md): `Student` and `StudentProfile`
- [One-to-many / many-to-one](onetomany.md): `Department` and `Course`
- [Many-to-many](manytomany.md): `Student` and `Course`
- [Annotation reference](annotations.md): entity, column, identifier, inheritance, queries, auditing, and caveats

The demo uses an in-memory H2 database, so no Docker or database setup is needed.
