package com.dozenflow.be.attachment;

/** Thrown when an uploaded file fails validation (too large, empty, or an unsupported type). */
public class InvalidAttachmentException extends RuntimeException {
    public InvalidAttachmentException(String message) {
        super(message);
    }
}
