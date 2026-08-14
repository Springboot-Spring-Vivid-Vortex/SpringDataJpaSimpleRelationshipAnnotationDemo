package com.example.jparelationships;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: fails if the Spring context (all entities, repositories,
 * controllers, and the DataLoader) cannot start up cleanly - e.g. a mapping
 * error in one of the entities, a missing bean, etc.
 */
@SpringBootTest
class JpaRelationshipDemoApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: the test passes if the ApplicationContext
        // (including DataLoader.run(), which exercises every relationship)
        // starts without throwing.
    }
}
