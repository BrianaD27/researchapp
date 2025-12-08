package com.vsu.researchapp.controller;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.service.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;



@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserAccountController {

    private final UserAccountService userService;

    public UserAccountController(UserAccountService userService) {
        this.userService = userService;
    }

    // ---------- 1) REAL REGISTER ENDPOINT (POST) ----------
    @PostMapping("/register")
    public ResponseEntity<UserAccount> registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "STUDENT") String role
    ) {
        UserAccount newUser = userService.createUser(username, email, password, role);
        return ResponseEntity.ok(newUser);
    }

    // ---------- 2) GET ALL USERS ----------
    @GetMapping
    public List<UserAccount> getAllUsers() {
        return userService.findAll();
    }

    // ---------- 3) GET ONE USER BY ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<UserAccount> getUserById(@PathVariable Long id) {
        Optional<UserAccount> userOpt = userService.findById(id);
        return userOpt.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ---------- 4) TEMP TEST ENDPOINT (no params) ----------
    // Hit this in a browser to quickly test DB wiring
    @GetMapping("/debug-create")
    public ResponseEntity<UserAccount> debugCreateUser() {
        UserAccount u = userService.createUser(
                "testuser",
                "testuser@example.com",
                "Password123!",
                "STUDENT"
        );
        return ResponseEntity.ok(u);
    }
}
