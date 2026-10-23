package com.vsu.researchapp.domain.repositoryinterfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageInterface {

    String store(MultipartFile file, String folder) throws IOException;
    
    void delete(String url);
}
