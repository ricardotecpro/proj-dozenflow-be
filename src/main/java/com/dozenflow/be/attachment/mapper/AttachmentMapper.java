package com.dozenflow.be.attachment.mapper;

import com.dozenflow.be.attachment.Attachment;
import com.dozenflow.be.attachment.AttachmentSummary;
import com.dozenflow.be.attachment.dto.AttachmentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponseDTO toResponseDTO(Attachment entity) {
        return new AttachmentResponseDTO(
                entity.getId(), entity.getFileName(), entity.getContentType(), entity.getSizeBytes(), entity.getCreatedAt()
        );
    }

    public AttachmentResponseDTO toResponseDTO(AttachmentSummary summary) {
        return new AttachmentResponseDTO(
                summary.getId(), summary.getFileName(), summary.getContentType(), summary.getSizeBytes(), summary.getCreatedAt()
        );
    }
}
