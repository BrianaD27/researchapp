package com.vsu.researchapp.domain.exception;

// Thrown when a file WAS valid (right type, right size, safe name) but something went wrong
// actually persisting or removing it:
//   - disk I/O error while writing to the local uploads folder (out of space, permissions, etc.)
//   - the Azure Blob Storage call fails (network issue, auth issue, container missing, etc.)
//
// Separate from InvalidFileException so the controller can map this to a 500 Internal Server
// Error instead of a 400 Bad Request -- the user didn't do anything wrong here, we did.
//
// TODO: extend RuntimeException like the other exceptions in this package.
// Consider also adding a second constructor that accepts a `cause` (Throwable), since storage
// failures usually wrap a lower-level exception (IOException, an Azure SDK exception, etc.)
// that you don't want to lose:
//
//     public FileStorageException(String message, Throwable cause) {
//         super(message, cause);
//     }
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }
}
