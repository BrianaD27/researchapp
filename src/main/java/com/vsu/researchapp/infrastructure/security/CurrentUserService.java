package com.vsu.researchapp.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;

// Resolves the UserAccount behind the JWT on the current request. JwtFilter only
// puts the username into the SecurityContext (see JwtFilter.doFilterInternal); this
// is the one place that turns "who is making this request" into an actual UserAccount
// row, so services can check ownership (e.g. "is this the student's own profile?")
// instead of trusting whatever id the client puts in the URL or request body.
@Service
public class CurrentUserService {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount getCurrentUserAccount() {
        String username = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        return userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException(
                "Authenticated user account not found: " + username));
    }

    public boolean isAdmin(UserAccount user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
