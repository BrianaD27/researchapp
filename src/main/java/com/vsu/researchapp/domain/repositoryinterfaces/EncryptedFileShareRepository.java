package com.vsu.researchapp.domain.repositoryinterfaces;
import com.vsu.researchapp.domain.model.EncryptedFileShare;
import com.vsu.researchapp.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncryptedFileShareRepository 
        extends JpaRepository<EncryptedFileShare, Long> {
    List<EncryptedFileShare> findBySharedWith(UserAccount user);
}
