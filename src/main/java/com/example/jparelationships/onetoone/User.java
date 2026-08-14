package com.example.jparelationships.onetoone;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Owning side of a bidirectional {@code @OneToOne} relationship.
 *
 * <p>Full write-up: {@code /doc/onetoone.md}
 *
 * <h2>Who "owns" the relationship?</h2>
 * In JPA, one side of every bidirectional association must be the
 * <b>owning side</b> - the side whose table actually holds the foreign key
 * column, and the only side Hibernate looks at when deciding what SQL
 * INSERT/UPDATE to run for the relationship.
 *
 * <p>Here, {@code users} is the owning side: its table has a
 * {@code profile_id} foreign-key column pointing at {@code user_profiles}.
 * That's why {@link #profile} is annotated with {@code @JoinColumn} - it
 * says "put the foreign key column here".
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * The owning side of the association.
     *
     * <ul>
     *   <li>{@code @OneToOne} - exactly one {@link UserProfile} per {@link User}.</li>
     *   <li>{@code @JoinColumn(name = "profile_id")} - creates a
     *       {@code profile_id} column in the {@code users} table that stores
     *       the {@code user_profiles.id} it points to, and a
     *       {@code UNIQUE} constraint on it (see {@code unique = true}) so
     *       the database itself enforces "one profile per user" and "one
     *       user per profile".</li>
     *   <li>{@code cascade = CascadeType.ALL} - persisting/removing a
     *       {@code User} also persists/removes its {@code UserProfile}.
     *       Convenient because a profile makes no sense without its user.</li>
     *   <li>{@code orphanRemoval = true} - if we ever replace or null-out
     *       this field on a managed {@code User}, the now-detached
     *       {@code UserProfile} row is deleted automatically.</li>
     *   <li>{@code fetch = FetchType.EAGER} (the {@code @OneToOne} default) -
     *       loading a {@code User} immediately loads its {@code UserProfile}
     *       in the same SQL query (via a JOIN). See the fetch-type nuance in
     *       {@code /doc/onetoone.md} - this default is the opposite of
     *       {@code @OneToMany}/{@code @ManyToMany}, which default to LAZY.</li>
     * </ul>
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", referencedColumnName = "id", unique = true)
    private UserProfile profile;

    protected User() {
        // Required by JPA.
    }

    public User(String username) {
        this.username = username;
    }

    /**
     * Convenience method that keeps both sides of the bidirectional
     * association in sync. Only the {@code profile} field on this (owning)
     * side actually affects the generated SQL, but keeping the in-memory
     * object graph consistent avoids confusing bugs before the entity is
     * ever flushed to the database.
     */
    public void assignProfile(UserProfile profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setUserInternal(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserProfile getProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "User{id=%d, username='%s'}".formatted(id, username);
    }
}
