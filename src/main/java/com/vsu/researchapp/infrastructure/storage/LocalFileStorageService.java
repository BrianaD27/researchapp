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
// ^ this bean only exists when file.storage.type=local (application-dev.properties);
//   it is skipped entirely when running with file.storage.type=azure
public class LocalFileStorageService implements FileStorageInterface {

    @Value("${file.storage.local.base-path}")
    // ^ injects "./uploads" from application-dev.properties into this field at startup
    private String basePath;

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        // folder is a logical bucket name, e.g. "profile-pictures" or "research-media"
        Path directory = Paths.get(basePath, folder);
        // ^ builds a Path like "./uploads/profile-pictures"

        try {
            Files.createDirectories(directory);
            // ^ creates the directory (and any missing parent directories) if it doesn't exist yet;
            //   no-ops silently if it already exists

            String originalFilename = file.getOriginalFilename();
            // ^ the filename the browser sent, e.g. "photo.png"

            String uniqueFilename = UUID.randomUUID() + "-" + originalFilename;
            // ^ prefixes a random UUID so two different users uploading "photo.png" never collide

            Path targetPath = directory.resolve(uniqueFilename);
            // ^ full path this file will be written to, e.g. "./uploads/profile-pictures/<uuid>-photo.png"

            file.transferTo(targetPath);
            // ^ writes the uploaded bytes from memory/temp storage to that path on disk

            return "/uploads/" + folder + "/" + uniqueFilename;
            // ^ the URL the frontend will use to load this file; only works because WebMvcConfig
            //   maps "/uploads/**" to this same basePath directory on disk

        } catch (IOException e) {
            // any failure above (can't create dir, can't write file, disk full, permissions, etc.)
            // gets normalized into our own exception type instead of leaking a raw IOException
            throw new FileStorageException(
                "Failed to store file on local disk: " + file.getOriginalFilename() + " - " + e.getMessage());
        }
    }

    @Override
    public void delete(String url) {
        // url looks like "/uploads/profile-pictures/<uuid>-photo.png"
        String relativePath = url.replaceFirst("^/uploads/", "");
        // ^ strips the leading "/uploads/" so we're left with "profile-pictures/<uuid>-photo.png"

        Path targetPath = Paths.get(basePath, relativePath);
        // ^ resolves that relative path back against basePath to get the real path on disk

        try {
            Files.deleteIfExists(targetPath);
            // ^ deletes the file; unlike Files.delete(), this does NOT throw if the file is
            //   already missing, which makes delete() safe to call more than once
        } catch (IOException e) {
            // wrap disk errors (permissions, file locked, etc.) the same way store() does
            throw new FileStorageException("Failed to delete file: " + url + " - " + e.getMessage());
        }
    }
}
