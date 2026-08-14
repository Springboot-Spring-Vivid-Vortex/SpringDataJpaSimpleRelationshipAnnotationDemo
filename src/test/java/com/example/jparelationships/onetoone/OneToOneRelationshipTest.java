package com.example.jparelationships.onetoone;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code @DataJpaTest} spins up an in-memory H2 database, the JPA/Hibernate
 * infrastructure, and rolls back the transaction after each test - a fast,
 * isolated way to verify entity mappings behave the way the comments in
 * {@link User}/{@link UserProfile} claim they do.
 */
@DataJpaTest
class OneToOneRelationshipTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void savingUserCascadesToItsProfile() {
        User user = new User("grace");
        user.assignProfile(new UserProfile("Grace Hopper", "Rear Admiral & programmer"));

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProfile().getId()).isNotNull();
        assertThat(userProfileRepository.findAll()).hasSize(1);
    }

    @Test
    void bothSidesOfTheAssociationNavigateToEachOther() {
        User user = new User("alan");
        UserProfile profile = new UserProfile("Alan Turing", "Codebreaker");
        user.assignProfile(profile);
        userRepository.save(user);

        User reloaded = userRepository.findByUsername("alan").orElseThrow();

        assertThat(reloaded.getProfile().getFullName()).isEqualTo("Alan Turing");
        assertThat(reloaded.getProfile().getUser()).isSameAs(reloaded);
    }

    @Test
    void orphanRemovalDeletesTheProfileWhenItIsDetached() {
        User user = new User("orphan-test");
        user.assignProfile(new UserProfile("Temp Name", "Temp bio"));
        userRepository.saveAndFlush(user);
        assertThat(userProfileRepository.findAll()).hasSize(1);

        user.assignProfile(null);
        userRepository.saveAndFlush(user);

        assertThat(userProfileRepository.findAll()).isEmpty();
    }
}
