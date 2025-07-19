package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDtoRequest;
import ru.practicum.comment.dto.CommentDtoResponse;

import java.util.List;

public interface CommentService {
    CommentDtoResponse getCommentAdmin(Long commentId);

    void deleteCommentAdmin(Long commentId);

    List<CommentDtoResponse> getCommentsOfEventPublic(Long eventId, Integer from, Integer size);

    List<CommentDtoResponse> getAllCommentsByUserIdPrivate(Long userId);

    CommentDtoResponse getCommentByIdByUserIdPrivate(Long userId, Long commentId);

    CommentDtoResponse createCommentPrivate(CommentDtoRequest newComment, Long userId, Long eventId);

    CommentDtoResponse updateCommentPrivate(CommentDtoRequest newComment, Long userId, Long commentId);

    void deleteByIdByUser(Long userId, Long commentId);
}