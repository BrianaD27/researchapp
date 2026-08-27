package com.vsu.researchapp.infrastructure.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.vsu.researchapp.domain.exception.FileStorageException; 
import com.vsu.researchapp.domain.repositoryinterfaces.FileStorageInterface; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.stereotype.Service; 
import org.springframework.web.multipart.MultipartFile; 

import java.io.IOException; 
import java.util.UUID; 

@Service 
@ConditionalOnProperty(name = "file.storage.type", havingValue = "azure")
public class AzureBlobStorageService implements FileStorageInterface {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.profile-picture-container}")
    private String profilePictureContainer;

    @Value("${azure.storage.research-media-container}")
    private String researchMediaContainer;

    public AzureBlobStorageService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        String containerName = "profile-pictures".equals(folder)
            ? profilePictureContainer
            : researchMediaContainer;

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            String originalFilename = file.getOriginalFilename();

            String blobName = UUID.randomUUID() + "-" + originalFilename;

            BlobClient blobClient = containerClient.getBlobClient(blobName);

            blobClient.upload(file.getInputStream(), file.getSize(), true);

            return blobClient.getBlobUrl();

        } catch (Exception e) {
   
            throw new FileStorageException(
                "Failed to store file in Azure Blob Storage: " + file.getOriginalFilename() + " - " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);

            String path = uri.getPath();

            String[] parts = path.replaceFirst("^/", "").split("/", 2);

            String containerName = parts[0];
            String blobName = parts[1];

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            BlobClient blobClient = containerClient.getBlobClient(blobName);

            blobClient.deleteIfExists();

        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file: " + url + " - " + e.getMessage(), e);
        }
    }
}
