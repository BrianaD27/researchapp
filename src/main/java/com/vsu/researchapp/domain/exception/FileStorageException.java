package com.vsu.researchapp.domain.exception;

// Thrown when a file WAS valid (right type, right size, safe name) but something went wrong
// actually persisting or removing it:
//   - disk I/O error while writing to the local uploads folder (out of space, permissions, etc.)
//   - the Azure Blob Storage call fails (network issue, auth issue, container missing, etc.)
//
// Separate from InvalidFileException so the controller can map this to a 500 Internal Server
// Error instead of a 400 Bad Request -- the user didn't do anything wrong here, we did.
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
