package com.dozenflow.be.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query("SELECT a.id as id, a.fileName as fileName, a.contentType as contentType, "
            + "a.sizeBytes as sizeBytes, a.createdAt as createdAt "
            + "FROM Attachment a WHERE a.task.id = :taskId ORDER BY a.createdAt ASC")
    List<AttachmentSummary> findSummariesByTaskId(@Param("taskId") Long taskId);

    long countByTaskId(Long taskId);
}
