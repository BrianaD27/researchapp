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
        Path directory = Paths.get(basePath, folder).toAbsolutePath().normalize();

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
        if (url == null || !url.startsWith("/uploads/")) {
            throw new IllegalArgumentException("Invalid upload URL");
        }

        Path uploadRoot = Paths.get(basePath).toAbsolutePath().normalize();
        String relativePath = url.substring("/uploads/".length());
        Path targetPath = uploadRoot.resolve(relativePath).normalize();

        if (!targetPath.startsWith(uploadRoot) || targetPath.equals(uploadRoot)) {
            throw new IllegalArgumentException("Invalid upload path");
        }

        try {
            Files.deleteIfExists(targetPath);

        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + url + " - " + e.getMessage(), e);
        }
    }
}
