package com.vsu.researchapp.domain.exception;

// Thrown when a file a user tries to upload is not acceptable BEFORE we ever try to store it:
//   - wrong MIME type (e.g. someone uploads a .exe as a "profile picture")
//   - file too large (over the configured max size)
//   - unsafe/invalid filename (path traversal attempts like "../../etc/passwd", null bytes, etc.)
//
// This is a "the input is bad" exception, as opposed to FileStorageException, which is
// "the input was fine but saving it failed". Keeping them separate makes it easier for the
// controller to return the right HTTP status (400 Bad Request here, vs 500 for storage failures).
//
// ProfessorNotFoundException in this same package. Give it a single-arg String constructor
// that just calls super(message).
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
