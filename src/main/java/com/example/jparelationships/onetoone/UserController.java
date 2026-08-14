package com.example.jparelationships.onetoone;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Minimal REST endpoint so you can inspect the {@code @OneToOne} data over
 * HTTP. Try: {@code GET http://localhost:8080/api/users}
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + id));
    }
}
