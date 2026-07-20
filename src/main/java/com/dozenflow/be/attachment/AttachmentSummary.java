package com.dozenflow.be.attachment;

import java.time.Instant;

/**
 * Spring Data projection used for listing: selects everything except the
 * `data` column, so the (up to 5MB) file bytes are never loaded just to
 * show a list of filenames.
 */
public interface AttachmentSummary {
    Long getId();
    String getFileName();
    String getContentType();
    long getSizeBytes();
    Instant getCreatedAt();
}
