package com.vsu.researchapp.domain.repositoryinterfaces;
import com.vsu.researchapp.domain.model.EncryptedFile;
import com.vsu.researchapp.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface EncryptedFileRepository extends JpaRepository<EncryptedFile, Long> {
    List<EncryptedFile> findByOwner(UserAccount owner);
}
