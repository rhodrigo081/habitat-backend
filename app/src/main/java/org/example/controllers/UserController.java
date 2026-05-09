package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.request.RegisterRequest;
import org.example.dtos.request.UserUpdateRequest;
import org.example.dtos.response.UserResponse;
import org.example.enums.UserRole;
import org.example.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findByIdResponse(id));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.findByUserRole(role));
    }

    @GetMapping("/coordinator/{coordinatorId}/estagiarios")
    public ResponseEntity<List<UserResponse>> getInternByCoordinator(
            @PathVariable Long coordinatorId) {
        return ResponseEntity.ok(userService.findInternByCoordinator(coordinatorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inactive(@PathVariable Long id) {
        userService.inactive(id);
        return ResponseEntity.noContent().build();
    }
}