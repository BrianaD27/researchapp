package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.LoginHistoryRepository;
import com.vsu.researchapp.domain.repository.PasswordResetTokenRepository;
import com.vsu.researchapp.domain.repository.RefreshTokenRepository;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import com.vsu.researchapp.infrastructure.externalServices.email.emailService;
import com.vsu.researchapp.infrastructure.security.JwtUtil;
import com.vsu.researchapp.infrastructure.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAccountServiceSecurityTest {

    private UserAccountRepository userRepository;
    private UserAccountService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserAccountRepository.class);
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        service = new UserAccountService(
            userRepository,
            new BCryptPasswordEncoder(),
            mock(JwtUtil.class),
            mock(LoginHistoryRepository.class),
            mock(PasswordResetTokenRepository.class),
            mock(RefreshTokenRepository.class),
            mock(emailService.class),
            mock(LoginAttemptService.class)
        );
    }

    @Test
    void publicStudentRoleIsNormalized() {
        UserAccount user = service.createUser(
            "student1", "student1@vsu.edu", "StrongPass1!", "STUDENT");

        assertEquals("ROLE_STUDENT", user.getRole());
    }

    @Test
    void arbitraryRolesAreRejected() {
        assertThrows(RuntimeException.class, () -> service.createUser(
            "attacker", "attacker@example.com", "StrongPass1!", "OWNER"));
    }

    @Test
    void unknownPasswordResetRequestDoesNotRevealAccountExistence() {
        service.generatePasswordResetToken("missing-user");

        verify(userRepository).findByUsername("missing-user");
    }
}
