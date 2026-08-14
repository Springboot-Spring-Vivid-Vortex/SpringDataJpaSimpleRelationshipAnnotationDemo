# `CascadeType` and `orphanRemoval`

Cascading answers one question: **"when I persist/remove this entity, what
should happen to the entities it references?"** It is entirely independent
of `fetch` (which controls *reading*, not *writing* - see
[fetch-types.md](fetch-types.md)) and independent of which side is
"owning" (cascade can technically be declared on either side, though it
almost always makes sense only on the side "closer to the parent").

## The six `CascadeType` values

| Value | Effect when applied to the parent operation |
|---|---|
| `PERSIST` | Saving the parent also saves (INSERTs) any new, not-yet-persisted children. |
| `MERGE` | Merging changes on the parent also merges changes on its children. |
| `REMOVE` | Deleting the parent also deletes its children. |
| `REFRESH` | Refreshing the parent from the database also refreshes its children. |
| `DETACH` | Detaching the parent from the persistence context also detaches its children. |
| `ALL` | All five of the above. |

Used in this project:

```java
// Department.employees
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)

// User.profile
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
```

Both use `CascadeType.ALL` because, in this model, an `Employee` without a
`Department` and a `UserProfile` without a `User` are meaningless - their
whole lifecycle should follow their parent's.

## ⚠️ Think twice before `CascadeType.ALL` / `REMOVE` in real systems

`CascadeType.REMOVE` (included in `ALL`) means **deleting the parent
deletes every child row too**. That's correct for "an employee record
can't outlive its department" in this teaching example, but would be a
serious bug in, say, an e-commerce system if `cascade = ALL` were put on
`Customer.orders` - deleting a customer account should almost certainly
*not* silently delete their entire order history. Always ask: "does this
child genuinely have no meaning without this specific parent?" before
reaching for `REMOVE`/`ALL`.

`CascadeType.MERGE`/`PERSIST` are usually much safer defaults to combine
than blanket `ALL`.

## `orphanRemoval = true` - the one thing cascade *can't* do alone

```java
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Employee> employees;
```

`orphanRemoval` handles a different situation than `CascadeType.REMOVE`:
what happens when a child is **removed from the collection** (or a
`@OneToOne`/`@ManyToOne` reference is nulled out) **without the parent
itself being deleted**?

```java
department.removeEmployee(grace);   // just detaches grace from this department
departmentRepository.save(department);
// -> orphanRemoval = true: grace's row is DELETED from the employees table
// -> orphanRemoval = false (default): grace's row keeps existing, but
//    department_id would need to become NULL - which fails here anyway
//    because Employee.department is @JoinColumn(nullable = false)!
```

Think of it as: `CascadeType.REMOVE` reacts to *deleting the parent*;
`orphanRemoval` reacts to *unlinking a child from its parent* while the
parent still exists. Both are demonstrated in
[`OneToManyRelationshipTest`](../src/test/java/com/example/jparelationships/onetomany/OneToManyRelationshipTest.java)
and [`OneToOneRelationshipTest`](../src/test/java/com/example/jparelationships/onetoone/OneToOneRelationshipTest.java).

`orphanRemoval` is only meaningful on `@OneToOne` and `@OneToMany` - never
on `@ManyToOne` or `@ManyToMany`, where a "child" can legitimately be
referenced by multiple parents at once, so unlinking it from *one* parent
is never a reason to delete it.

## No `cascade` attribute on `@ElementCollection`

As explained in [elementcollection.md](elementcollection.md), an
`@ElementCollection`'s rows have no independent existence at all - they
always, unconditionally, mirror the current Java collection. There's no
`cascade` setting because there's no "don't cascade" option to opt out of.
