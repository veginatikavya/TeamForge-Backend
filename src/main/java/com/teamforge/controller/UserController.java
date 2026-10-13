package com.teamforge.controller;

import com.teamforge.entity.User;
import com.teamforge.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // CREATE CANDIDATE
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {

        try {
            User savedUser = userService.saveUser(user);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // GET ALL CANDIDATES
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET CANDIDATE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {

        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // UPDATE CANDIDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser
    ) {

        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Candidate not found."));
        }

        try {
            User savedUser = userService.updateUser(id, updatedUser);

            return ResponseEntity.ok(savedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE CANDIDATE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Candidate not found."));
        }

        try {
            userService.deleteUser(id);

            return ResponseEntity.ok(
                    Map.of("message", "Candidate deleted successfully.")
            );

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Cannot delete this candidate because they may be assigned to a project team."
                    ));
        }
    }
}