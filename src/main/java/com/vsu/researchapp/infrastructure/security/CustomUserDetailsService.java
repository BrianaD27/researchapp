package com.vsu.researchapp.infrastructure.security;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.UserAccountRepository;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Block disabled accounts
        if (!acc.isActive()) {
            throw new DisabledException("Account is disabled");
        }

        // Block locked accounts (Feature #1)
        if (acc.isLocked()) {
            throw new LockedException("Account locked until " + acc.getLockUntil());
        }

        // ✅ Feature #2: real roles (USER vs ADMIN)
        String role = acc.getRole();
        if (role == null || role.isBlank()) {
            role = "USER"; // default
        }

        // Spring Security expects ROLE_*
        String securityRole = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(securityRole);

        return new org.springframework.security.core.userdetails.User(
                acc.getUsername(),
                acc.getPasswordHash(),
                List.of(authority)
        );
    }
}
