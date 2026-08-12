package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.EncryptedFile;
import com.vsu.researchapp.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncryptedFileRepository extends JpaRepository<EncryptedFile, Long> {
    List<EncryptedFile> findByOwner(UserAccount owner);
}
