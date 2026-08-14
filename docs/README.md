# Spring Data JPA learning guide

This folder explains the annotations used by the runnable sample. You do not need database knowledge before starting: read the guides in order, then use `annotations.md` as a reference.

## A tiny mental model

Imagine a database as several spreadsheets. A Java **entity** is one row represented as an object. A relationship says how a row in one spreadsheet points to rows in another spreadsheet. For example, one course row has a `department_id` cell that points to its department row.

- [One-to-one](onetone.md): `Student` and `StudentProfile`
- [One-to-many](onetomany.md): `Department` and its `courses` collection
- [Many-to-one](manytoone.md): `Course` and its `department` foreign key
- [Many-to-many](manytomany.md): `Student` and `Course`
- [Relationship vocabulary and choosing a mapping](relationships.md)
- [Annotation reference](annotations.md): entity, column, identifier, inheritance, queries, auditing, and caveats

The demo uses an in-memory H2 database, so no Docker or database setup is needed.

## Recommended reading order

1. [Relationship vocabulary](relationships.md) — learn the small set of words first.
2. [One-to-one](onetone.md) — the smallest relationship.
3. [Many-to-one](manytoone.md) — the most common database relationship.
4. [One-to-many](onetomany.md) — the matching collection view.
5. [Many-to-many](manytomany.md) — a relationship that needs a middle table.
6. [Annotation reference](annotations.md) — the wider JPA and Spring Data toolkit.

## How to use a guide

Each relationship guide intentionally introduces only one main idea first. Read its small example, then its mapping, then the production advice. Do not try to memorize every attribute at once. The important progression is:

`table column` → `Java field` → `owning side` → `loading and lifecycle options` → `production query behaviour`

That progression mirrors how an experienced Java/Spring engineer should reason about a JPA mapping.
