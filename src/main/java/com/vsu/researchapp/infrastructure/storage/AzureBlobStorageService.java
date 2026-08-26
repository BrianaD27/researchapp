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
// ^ this bean only exists when file.storage.type=azure (application-prod.properties);
//   it is skipped entirely in dev, so a missing Azure connection string never breaks local runs
public class AzureBlobStorageService implements FileStorageInterface {

    private final BlobServiceClient blobServiceClient;
    // ^ injected from the bean defined in StorageConfig; this is the entry point to the whole
    //   Azure Storage account (built from the connection string)

    @Value("${azure.storage.profile-picture-container}")
    // ^ injects "profile-pictures" from application-prod.properties
    private String profilePictureContainer;

    @Value("${azure.storage.research-media-container}")
    // ^ injects "research-media" from application-prod.properties
    private String researchMediaContainer;

    public AzureBlobStorageService(BlobServiceClient blobServiceClient) {
        // constructor injection: Spring passes in the BlobServiceClient bean automatically,
        // same pattern used by StudentService/ProfessorService for their repository dependencies
        this.blobServiceClient = blobServiceClient;
    }

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        String containerName = "profile-pictures".equals(folder)
            ? profilePictureContainer
            : researchMediaContainer;
        // ^ picks which container to upload into based on the logical folder name passed in

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            // ^ gets a client scoped to that one container (created ahead of time in the Azure Portal)

            String originalFilename = file.getOriginalFilename();
            // ^ the filename the browser sent, e.g. "photo.png"

            String blobName = UUID.randomUUID() + "-" + originalFilename;
            // ^ prefixes a random UUID so two different users' uploads never collide, same idea
            //   as the local disk implementation

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            // ^ gets a client scoped to this one specific blob (file) inside the container

            blobClient.upload(file.getInputStream(), file.getSize(), true);
            // ^ streams the uploaded bytes to Azure; the trailing "true" means overwrite-if-exists,
            //   which is harmless here since blobName is already unique

            return blobClient.getBlobUrl();
            // ^ the public URL of the uploaded blob; only directly usable by the frontend if the
            //   container's public access level allows anonymous blob reads

        } catch (Exception e) {
            // any failure above (network issue, auth issue, container missing, etc.) gets
            // normalized into our own exception type instead of leaking an Azure SDK exception
            throw new FileStorageException(
                "Failed to store file in Azure Blob Storage: " + file.getOriginalFilename() + " - " + e.getMessage());
        }
    }

    @Override
    public void delete(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            // ^ parses the blob URL, e.g. "https://<account>.blob.core.windows.net/<container>/<blobName>"

            String path = uri.getPath();
            // ^ everything after the host, e.g. "/research-media/<uuid>-slides.pptx"

            String[] parts = path.replaceFirst("^/", "").split("/", 2);
            // ^ strips the leading "/" then splits into exactly two pieces: container name and blob name

            String containerName = parts[0];
            String blobName = parts[1];

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            // ^ gets a client scoped to the container this blob lives in

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            // ^ gets a client scoped to the specific blob to delete

            blobClient.deleteIfExists();
            // ^ deletes the blob; like Files.deleteIfExists(), this won't throw if it's already gone,
            //   which makes delete() safe to call more than once

        } catch (Exception e) {
            // wrap parsing errors or Azure SDK failures the same way store() does
            throw new FileStorageException("Failed to delete file: " + url + " - " + e.getMessage());
        }
    }
}
