package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoPublic;

import java.util.List;

public interface CommentService {

    CommentDto addCommentToEvent(Long authorId, Long eventId, CommentDto commentDto);

    CommentDto getCommentByUser(Long authorId, Long commentId);

    List<CommentDto> getAllCommentsByEvent(Long eventId);

    CommentDto updateCommentByUser(Long authorId, Long commentId, CommentDto commentDto);

    void deleteCommentByUser(Long authorId, Long commentId);


    CommentDto updateCommentByAdmin(Long commentId, CommentDto commentDto);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDtoPublic> getAllCommentsByEventPublic(Long eventId);
}
