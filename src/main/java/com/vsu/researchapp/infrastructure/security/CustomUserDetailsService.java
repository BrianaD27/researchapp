package com.vsu.researchapp.infrastructure.security;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.UserAccountRepository;

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

        // For now, treat everyone as ADMIN until we wire real roles
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_ADMIN");

       return new org.springframework.security.core.userdetails.User(
        acc.getUsername(),
        acc.getPasswordHash(),
        List.of(authority)
);

    }
}
