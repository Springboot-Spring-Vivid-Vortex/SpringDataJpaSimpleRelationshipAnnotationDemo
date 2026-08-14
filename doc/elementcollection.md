# `@ElementCollection`

**Example:** [`Employee.skills`](../src/main/java/com/example/jparelationships/onetomany/Employee.java)

```java
@ElementCollection
@CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
@Column(name = "skill")
private List<String> skills = new ArrayList<>();
```

### The schema this produces

```
employee_skills
+-------------+-----------------+
| employee_id | skill           |
+-------------+-----------------+
| 1           | COBOL           |
| 1           | Naval Engineering|
+-------------+-----------------+
```

## What it's for

A collection of **basic types** (`String`, `Integer`, an `enum`, ...) or
`@Embeddable` types, owned entirely by one entity, where the elements have
no identity of their own and no reason to ever be an independent `@Entity`.
"A list of skill names" doesn't need a `Skill` entity with an id and a
repository - it's not something other employees reference or share.

- `@ElementCollection` marks the collection.
- `@CollectionTable` names the table that stores it and the foreign key
  column back to the owner (`employee_id`). Without it, Hibernate picks a
  default table/column name.
- `@Column(name = "skill")` names the column holding the actual value
  (otherwise defaults to something like `skills`).

## How it's different from `@OneToMany`

| | `@ElementCollection` | `@OneToMany` |
|---|---|---|
| Element type | Basic type or `@Embeddable` | `@Entity` |
| Element has its own id? | No | Yes |
| Element has its own repository? | No | Yes |
| Cascade behavior | Always fully owned - inserts/updates/deletes automatically follow the owner, no `cascade` attribute to configure | Configurable via `cascade` |
| Fetch default | `LAZY` | `LAZY` |

There is no `cascade` attribute on `@ElementCollection` because the
elements have no independent existence to *not* cascade to - the child
table's rows are entirely a reflection of the Java collection's current
contents, always. Add a string to `skills` and save: a row is inserted.
Remove a string and save: its row is deleted. There's no separate
"detach without deleting" concept, unlike `orphanRemoval` for `@OneToMany`
(see [cascade-types.md](cascade-types.md)).

## `@ElementCollection` of an `@Embeddable`

The value type doesn't have to be a plain `String`/`Integer` - it can be an
`@Embeddable` too, e.g. `List<Address> previousAddresses` would create an
`employee_previous_addresses` table with `employee_id`, `street`, `city`,
etc. columns (all embeddable fields, plus the owner's foreign key) - not
demonstrated in this project's code to keep things focused, but works
exactly the same way conceptually.

## `Map` variant

`@ElementCollection` also works on a `Map<K, V>` (e.g.
`Map<String, String> customFields`), producing a table with columns for the
owner's foreign key, the map key, and the map value. Not used here, but
worth knowing it exists.
