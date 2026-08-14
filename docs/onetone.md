# `@OneToOne`: one student, one profile

In this project, each `Student` can have one `StudentProfile`, and each profile belongs to one student.

```java
// Student: owning side
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "profile_id", unique = true)
private StudentProfile profile;

// StudentProfile: inverse side
@OneToOne(mappedBy = "profile")
private Student student;
```

`@JoinColumn` creates the foreign-key column. Because it is on `Student`, `Student` is the **owning side**. Only changes made through `student.setProfile(...)` are persisted as relationship changes. `mappedBy` says “the other field owns this relationship”; it does not create another column.

`unique = true` is important: without it, the database would allow multiple students to point at the same profile, which is many-to-one rather than one-to-one.

`CascadeType.ALL` means saving or deleting the student also saves or deletes its profile. `orphanRemoval = true` means replacing or clearing the profile deletes the old profile. Use both only when the profile has no independent life. `LAZY` delays loading the profile until `getProfile()` is called; access it inside a transaction or fetch it explicitly to avoid a lazy-loading exception.
