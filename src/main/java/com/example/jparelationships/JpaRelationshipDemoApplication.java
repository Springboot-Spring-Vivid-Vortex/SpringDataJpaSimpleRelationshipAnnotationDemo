package com.example.jparelationships;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the demo application.
 *
 * <p>This project exists purely to teach Spring Data JPA's relationship
 * annotations by example. Each package under {@code com.example.jparelationships}
 * is a self-contained mini-demo:
 *
 * <ul>
 *   <li>{@code onetoone}   - {@code @OneToOne}   (User &lt;-&gt; UserProfile)</li>
 *   <li>{@code onetomany}  - {@code @OneToMany} / {@code @ManyToOne} (Department &lt;-&gt; Employee),
 *                            plus {@code @Embeddable}/{@code @Embedded} and {@code @ElementCollection}</li>
 *   <li>{@code manytomany} - {@code @ManyToMany} (Student &lt;-&gt; Course), plus a
 *                            "many-to-many with extra columns" example using
 *                            {@code @EmbeddedId} (CourseEnrollment)</li>
 * </ul>
 *
 * <p>Read the matching Markdown file under {@code /doc} for a full
 * explanation of the concepts demonstrated in each package.
 *
 * <p>On startup, {@link com.example.jparelationships.config.DataLoader} seeds
 * sample data and prints a walkthrough of each relationship to the console.
 */
@SpringBootApplication
public class JpaRelationshipDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaRelationshipDemoApplication.class, args);
    }
}
