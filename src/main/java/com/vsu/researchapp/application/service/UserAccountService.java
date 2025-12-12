package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    // 1) Create a new user (used by /register)
    public UserAccount createUser(String username, String email, String password, String role) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);  // hash complete
        user.setRole(role);
        return userAccountRepository.save(user);
    }

    // 2) Get all users
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    // 3) Get one user by id
    public Optional<UserAccount> findById(Long id) {
        return userAccountRepository.findById(id);
    }
}
