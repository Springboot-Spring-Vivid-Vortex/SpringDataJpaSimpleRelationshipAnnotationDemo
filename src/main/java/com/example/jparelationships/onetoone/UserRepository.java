package com.example.jparelationships.onetoone;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA generates the implementation of this interface at runtime.
 * We get {@code save}, {@code findById}, {@code findAll}, {@code delete}, ...
 * for free just by extending {@link JpaRepository}.
 *
 * <p>{@code findByUsername} is a "derived query method": Spring Data parses
 * the method name and builds the {@code WHERE username = ?1} query for us -
 * no SQL or JPQL required.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
