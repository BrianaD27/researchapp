package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.service.UserAccountService;
import com.vsu.researchapp.domain.model.UserAccount;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserAccountService userAccountService;

    public AdminController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    // GET all users — admin only
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAccount>> getAllUsers() {
        return ResponseEntity.ok(userAccountService.findAll());
    }

    // PUT assign role to user — admin only
    @PutMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRole(
            @PathVariable String username,
            @RequestParam String role) {

        UserAccount user = userAccountService
            .assignRole(username, role);

        return ResponseEntity.ok(Map.of(
            "message", "Role updated successfully",
            "username", user.getUsername(),
            "role", user.getRole()
        ));
    }

    // PUT activate user — admin only
    @PutMapping("/users/{username}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(
            @PathVariable String username) {

        UserAccount user = userAccountService
            .setActiveStatus(username, true);

        return ResponseEntity.ok(Map.of(
            "message", "User activated",
            "username", user.getUsername()
        ));
    }

    // PUT deactivate user — admin only
    @PutMapping("/users/{username}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUser(
            @PathVariable String username) {

        UserAccount user = userAccountService
            .setActiveStatus(username, false);

        return ResponseEntity.ok(Map.of(
            "message", "User deactivated",
            "username", user.getUsername()
        ));
    }

    // PUT unlock user — admin only
    @PutMapping("/users/{username}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unlockUser(
            @PathVariable String username) {

        UserAccount user = userAccountService
            .unlockAccount(username);

        return ResponseEntity.ok(Map.of(
            "message", "User account unlocked",
            "username", user.getUsername()
        ));
    }
}
