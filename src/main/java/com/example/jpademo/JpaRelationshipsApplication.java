package com.example.jpademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point. Run this application to create the example schema in the H2 database. */
@SpringBootApplication
public class JpaRelationshipsApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaRelationshipsApplication.class, args);
    }
}
