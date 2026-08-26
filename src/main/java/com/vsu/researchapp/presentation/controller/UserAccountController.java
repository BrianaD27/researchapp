package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.dto.UserAccountDto;
import com.vsu.researchapp.application.service.UserAccountService;
import com.vsu.researchapp.domain.model.UserAccount;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserAccountController {

    private final UserAccountService userService;

    public UserAccountController(UserAccountService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserAccountDto> registerUser(
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank String email,
            @RequestParam @NotBlank String password,
            @RequestParam(defaultValue = "STUDENT") String role) {
        UserAccount newUser = userService.createUser(
            username, email, password, role);
        return ResponseEntity.ok(toDto(newUser));
    }

    // Paginated list of users
    @GetMapping
    public ResponseEntity<Page<UserAccountDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username") String sortBy) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(sortBy).ascending());
        return ResponseEntity.ok(userService.findAllPaginated(pageable).map(this::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAccountDto> getUserById(
            @PathVariable Long id) {
        Optional<UserAccount> userOpt = userService.findById(id);
        return userOpt.map(user -> ResponseEntity.ok(toDto(user)))
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/login-history")
    public ResponseEntity<?> getLoginHistory(
            @PathVariable String username) {
        return ResponseEntity.ok(
            userService.getLoginHistory(username));
    }

    private UserAccountDto toDto(UserAccount user) {
        return new UserAccountDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.isActive(),
            user.isAccountLocked(),
            user.isTwoFactorEnabled(),
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
