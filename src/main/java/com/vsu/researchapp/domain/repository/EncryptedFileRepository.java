package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.EncryptedFile;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncryptedFileRepository extends JpaRepository<EncryptedFile, Long> {

    List<EncryptedFile> findByOwner(UserAccount owner);

    List<EncryptedFile> findByResearchOpportunity(ResearchOpportunity opportunity);
}
