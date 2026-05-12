package com.vsu.researchapp.infrastructure.security;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.UserAccountRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository repo;

    public CustomUserDetailsService(UserAccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UserAccount acc = repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found"));

        // Auto-unlock after 15 minutes
        if (acc.isAccountLocked() && acc.getLockTime() != null &&
                acc.getLockTime().isBefore(
                    LocalDateTime.now().minusMinutes(15))) {
            acc.setAccountLocked(false);
            acc.setFailedAttempts(0);
            repo.save(acc);
        }

        // Use the REAL role from the database
        String role = acc.getRole() != null ? 
            acc.getRole() : "ROLE_STUDENT";

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        SimpleGrantedAuthority authority = 
            new SimpleGrantedAuthority(role);

        return org.springframework.security.core.userdetails
            .User.withUsername(acc.getUsername())
            .password(acc.getPasswordHash())
            .authorities(List.of(authority))
            .disabled(!acc.isActive())
            .accountLocked(acc.isAccountLocked())
            .build();
    }
}