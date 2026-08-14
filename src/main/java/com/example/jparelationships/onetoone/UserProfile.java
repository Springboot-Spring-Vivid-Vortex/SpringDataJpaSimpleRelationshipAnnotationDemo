package com.example.jparelationships.onetoone;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

/**
 * Inverse ("mappedBy") side of the bidirectional {@code @OneToOne} with
 * {@link User}.
 *
 * <p>Full write-up: {@code /doc/onetoone.md}
 *
 * <h2>Why {@code mappedBy}?</h2>
 * {@code mappedBy = "profile"} tells Hibernate "I do not own a foreign key
 * column for this relationship - go look at the {@code profile} field on
 * {@link User} instead". This side is purely for convenience navigation
 * ({@code userProfile.getUser()}); changing only this field on a managed
 * entity has <b>no effect</b> on the generated SQL.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String bio;

    /**
     * {@code mappedBy = "profile"} matches the field name of the owning
     * side ({@link User#profile}). No {@code @JoinColumn} here - this table
     * ({@code user_profiles}) has no foreign key column at all.
     *
     * <p>{@code @JsonBackReference} pairs with {@code @JsonManagedReference}
     * used elsewhere in this project to stop Jackson from recursing forever
     * (User -> profile -> user -> profile -> ...) when serializing to JSON.
     * See {@code /doc/json-serialization.md}.
     */
    @OneToOne(mappedBy = "profile")
    @JsonBackReference
    private User user;

    protected UserProfile() {
        // Required by JPA.
    }

    public UserProfile(String fullName, String bio) {
        this.fullName = fullName;
        this.bio = bio;
    }

    /** Package-private setter used only by {@link User#assignProfile}. */
    void setUserInternal(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBio() {
        return bio;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserProfile{id=%d, fullName='%s'}".formatted(id, fullName);
    }
}
