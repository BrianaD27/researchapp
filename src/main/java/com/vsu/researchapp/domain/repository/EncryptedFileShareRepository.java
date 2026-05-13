package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.EncryptedFileShare;
import com.vsu.researchapp.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncryptedFileShareRepository 
        extends JpaRepository<EncryptedFileShare, Long> {
    List<EncryptedFileShare> findBySharedWith(UserAccount user);
}
