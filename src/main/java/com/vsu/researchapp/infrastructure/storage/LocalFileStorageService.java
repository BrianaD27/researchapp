package com.vsu.researchapp.infrastructure.storage;

import com.vsu.researchapp.domain.exception.FileStorageException; 
import com.vsu.researchapp.domain.repositoryinterfaces.FileStorageInterface;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.stereotype.Service; 
import org.springframework.web.multipart.MultipartFile; 

import java.io.IOException; 
import java.nio.file.Files; 
import java.nio.file.Path; 
import java.nio.file.Paths; 
import java.util.UUID; 

@Service 
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local")
public class LocalFileStorageService implements FileStorageInterface {

    @Value("${file.storage.local.base-path}")
    private String basePath;

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        Path directory = Paths.get(basePath, folder);

        try {
            Files.createDirectories(directory);
    
            String originalFilename = file.getOriginalFilename();
        
            String uniqueFilename = UUID.randomUUID() + "-" + originalFilename;
          
            Path targetPath = directory.resolve(uniqueFilename);
          
            file.transferTo(targetPath);
          
            return "/uploads/" + folder + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new FileStorageException(
                "Failed to store file on local disk: " + file.getOriginalFilename() + " - " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String url) {
        String relativePath = url.replaceFirst("^/uploads/", "");
        Path targetPath = Paths.get(basePath, relativePath);

        try {
            Files.deleteIfExists(targetPath);
        
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + url + " - " + e.getMessage(), e);
        }
    }
}
