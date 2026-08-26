package com.vsu.researchapp.domain.repositoryinterfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// This is a "port" interface, same idea as StudentRepositoryInterface/ProfessorRepositoryInterface.
// The application layer (MediaUploadService) will only ever talk to THIS interface, never to a
// specific storage technology directly. That's what lets us swap in LocalFileStorageService for
// dev and AzureBlobStorageService for prod without touching any business logic.
public interface FileStorageInterface {

    // Stores the given file under a logical "folder" (e.g. "profile-pictures" or "research-media")
    // and returns a URL/path that can be saved onto profilePictureUrl or researchMediaUrls and
    // later used by the frontend to actually load/display the file.
    //
    // Implementations differ a lot here:
    //   - LocalFileStorageService writes bytes to disk under a base upload directory and returns
    //     something like "/uploads/profile-pictures/<uuid>-<filename>".
    //   - AzureBlobStorageService uploads bytes to a Blob container and returns the blob's URL
    //     (or a SAS URL if the container isn't publicly readable).
    //
    // throws IOException because reading MultipartFile bytes / writing to disk or network can fail.
    String store(MultipartFile file, String folder) throws IOException;

    // Deletes a previously stored file, given the same URL/path that store() returned.
    // Used when a user replaces their profile picture (delete the old one) or removes a media
    // item from a research opportunity.
    //
    // Implementations need to figure out, from the URL, what to actually delete:
    //   - Local: strip the "/uploads/" prefix back down to a file path on disk.
    //   - Azure: parse out the blob name from the URL to know which blob to delete.
    void delete(String url);
}
