package com.dozenflow.be.comment.mapper;

import com.dozenflow.be.comment.Comment;
import com.dozenflow.be.comment.dto.CommentRequestDTO;
import com.dozenflow.be.comment.dto.CommentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public Comment toEntity(CommentRequestDTO dto) {
        Comment comment = new Comment();
        comment.setBody(dto.body());
        return comment;
    }

    public CommentResponseDTO toResponseDTO(Comment entity) {
        return new CommentResponseDTO(entity.getId(), entity.getBody(), entity.getCreatedAt());
    }
}
