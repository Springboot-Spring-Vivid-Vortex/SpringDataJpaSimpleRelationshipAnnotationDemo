# `@OneToOne`

**Example:** [`User`](../src/main/java/com/example/jparelationships/onetoone/User.java) &lt;-&gt; [`UserProfile`](../src/main/java/com/example/jparelationships/onetoone/UserProfile.java)

Every `User` has exactly one `UserProfile`, and every `UserProfile` belongs
to exactly one `User`.

## The schema this produces

```
users                          user_profiles
+----+----------+------------+ +----+-----------+--------------------+
| id | username | profile_id | | id | full_name | bio                |
+----+----------+------------+ +----+-----------+--------------------+
| 1  | ada      | 1          | | 1  | Ada Lov.. | Mathematician & .. |
+----+----------+------------+ +----+-----------+--------------------+
```

`users.profile_id` is a foreign key into `user_profiles.id`, with a
`UNIQUE` constraint - that uniqueness is what turns an otherwise
many-to-one-shaped foreign key into a genuine one-to-one.

## The owning side (`User.profile`)

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
@JoinColumn(name = "profile_id", referencedColumnName = "id", unique = true)
private UserProfile profile;
```

- `@JoinColumn` is what makes this the **owning side**: it says "the
  `users` table gets a `profile_id` column". Only changes to this field
  cause `INSERT`/`UPDATE`/`DELETE` statements to run for the relationship.
- `unique = true` is what makes this "one-to-one" rather than
  "many-to-one" at the database level.
- `fetch = FetchType.EAGER` is the **default** for `@OneToOne` (and
  `@ManyToOne`) - loading a `User` immediately joins in its `UserProfile`.
  Compare to `@OneToMany`/`@ManyToMany`, which default to `LAZY`. See
  [fetch-types.md](fetch-types.md).

## The inverse side (`UserProfile.user`)

```java
@OneToOne(mappedBy = "profile")
private User user;
```

- `mappedBy = "profile"` points back at the *field name* on `User`
  (`profile`), telling Hibernate "don't create a column for this - the
  other side already owns it". There is deliberately no `@JoinColumn` here;
  the `user_profiles` table has no foreign key column at all.
- Purely for convenient Java navigation (`profile.getUser()`). Setting
  this field alone, without ever touching `User.profile`, would silently
  do nothing in the database.

## Unidirectional alternative

If you never need `profile.getUser()`, you can delete the `user` field
from `UserProfile` (and its `@OneToOne(mappedBy=...)`) entirely and keep
only `User.profile`. That's a **unidirectional** `@OneToOne` - simpler
when you genuinely never need to navigate backwards.

## Nuance: shared primary key style (`@MapsId`)

This project's `User`/`UserProfile` uses a *dedicated* foreign-key column
(`profile_id`). A common alternative is to make `UserProfile.id` **be**
`User.id` (no separate `profile_id` column at all) using `@MapsId`:

```java
@Entity
class UserProfile {
    @Id
    private Long id; // same value as the owning User's id

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
}
```

This guarantees a strict 1:1 by construction (they literally share a
primary key) and saves a column, at the cost of `UserProfile` no longer
having an independent id sequence. See `CourseEnrollment` in
[manytomany.md](manytomany.md) for another, fully-worked `@MapsId` example.

## Nuance: keep both sides in sync yourself

JPA never keeps a bidirectional association in sync in Java memory for
you - `user.assignProfile(profile)` in this project manually calls
`profile.setUserInternal(user)` so `profile.getUser()` works correctly
*before* the objects are ever saved/reloaded. Forgetting this is one of
the most common sources of "it works after a restart but not in this test"
bugs.
