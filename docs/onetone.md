# `@OneToOne`: one student, one profile

## In simple terms

In this project, each `Student` can have one `StudentProfile`, and each profile belongs to one student.

Think of a student profile as an extension of a student record: one student has one phone-number profile, and that profile should not be shared by another student.

## Small practical example

```java
Student asha = new Student("Asha");
asha.setProfile(new StudentProfile("555-0100"));
studentRepository.save(asha);
```

Saving Asha also saves the profile because this particular relationship uses cascading.

## Mapping, line by line

```java
// Student: owning side
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "profile_id", unique = true)
private StudentProfile profile;

// StudentProfile: inverse side
@OneToOne(mappedBy = "profile")
private Student student;
```

`@OneToOne` says one object points to one object. `@JoinColumn` creates the foreign-key column, `students.profile_id`. Because it is on `Student`, `Student` is the **owning side**. “Owner” only means “the field JPA writes to the database”; it does not mean the student is more important. Only changes made through `student.setProfile(...)` are persisted as relationship changes. `mappedBy` says “the other field already owns this relationship”, so it does not create another column.

`unique = true` is important: without it, the database would allow multiple students to point at the same profile, which is many-to-one rather than one-to-one.

## Why these options are used

`CascadeType.ALL` means saving or deleting the student also saves or deletes its profile. `orphanRemoval = true` means replacing or clearing the profile deletes the old profile. This is suitable only because the demo profile has no independent life. `LAZY` delays loading the profile until `getProfile()` is called; access it inside a transaction or fetch it explicitly to avoid a lazy-loading exception.

## Common mistakes and production advice

- Forgetting `unique = true` makes the database mapping effectively many-to-one.
- Updating only `profile.setStudent(student)` changes the inverse side and does not save the foreign key. Use `student.setProfile(profile)`.
- Do not cascade deletes if profiles are shared or must be retained for audit/history.
- A shared-primary-key one-to-one is another variation, using `@MapsId`; it is more tightly coupled and unnecessary for this beginner example.

## Interview Answer

**How do you map a one-to-one relationship in JPA?** Put `@OneToOne` and `@JoinColumn` on the owning side, and ensure the foreign key is unique. Add `mappedBy` on the inverse side for a bidirectional mapping. Choose cascade and orphan removal only when the child truly shares the parent’s lifecycle.
