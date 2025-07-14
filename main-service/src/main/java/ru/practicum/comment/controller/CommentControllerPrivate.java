package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDtoRequest;
import ru.practicum.comment.dto.CommentDtoResponse;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class CommentControllerPrivate {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDtoResponse>> getComments(@PathVariable Long userId) {
        log.info("Эндпоинт /users/{}/comments/.GET запрос на получение всех комментариев пользователя.", userId);
        return new ResponseEntity<>(commentService.getAllCommentsByUserIdPrivate(userId), HttpStatus.OK);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDtoResponse> getComment(@PathVariable Long userId,
                                                         @PathVariable Long commentId) {
        log.info("Эндпоинт /users/{}/comments/{}. GET запрос  на получение комментария события.", userId,
                commentId);
        return new ResponseEntity<>(commentService.getCommentByIdByUserIdPrivate(userId, commentId), HttpStatus.OK);
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<CommentDtoResponse> createComment(@RequestBody @Valid CommentDtoRequest newComment,
                                                            @PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Эндпоинт /users/{}/comments/{}. POST запрос на создание нового комментария {} для события.", userId,
                eventId, newComment);
        return new ResponseEntity<>(commentService.createCommentPrivate(newComment, userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDtoResponse> updateComment(@RequestBody @Valid CommentDtoRequest newComment,
                                                            @PathVariable Long userId, @PathVariable Long commentId) {
        log.info("Эндпоинт /users/{}/comments/{}. PATCH запрос  на обновление комментария для события" +
                        " на новый комментарий {}.",
                userId, commentId, newComment);
        return new ResponseEntity<>(commentService.updateCommentPrivate(newComment, userId, commentId), HttpStatus.OK);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteByIdByUser(userId, commentId);
        log.info("Эндпоинт /users/{}/comments/{}. DELETE запрос по  на удаление комментария с id {}.",
                userId, commentId, commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}