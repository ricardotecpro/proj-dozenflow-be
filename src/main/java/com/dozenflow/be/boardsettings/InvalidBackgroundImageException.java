package com.dozenflow.be.boardsettings;

/** Thrown when an uploaded background image fails validation (too large, empty, or an unsupported type). */
public class InvalidBackgroundImageException extends RuntimeException {
    public InvalidBackgroundImageException(String message) {
        super(message);
    }
}
