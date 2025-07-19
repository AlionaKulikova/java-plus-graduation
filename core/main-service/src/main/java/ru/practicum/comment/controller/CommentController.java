package ru.practicum.comment.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoPublic;
import ru.practicum.comment.service.CommentService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/users/{userId}/events/{eventId}/comments")
    public ResponseEntity<CommentDto> addCommentToEvent(@NonNull @PathVariable("userId") Long authorId,
                                                        @NonNull @PathVariable("eventId") Long eventId,
                                                        @Valid @RequestBody CommentDto commentDto) {
        log.info("Вызов метода добавление комментария (addCommentToEvent)");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addCommentToEvent(authorId, eventId, commentDto));
    }

    @GetMapping("/users/{userId}/comments/{commentId}")
    public ResponseEntity<CommentDto> getCommentByUser(@NonNull @PathVariable("userId") Long authorId,
                                                       @NonNull @PathVariable("commentId") Long commentId) {
        log.info("Вызов метода получение комментария с ID = {} пользователем с ID = {} (getCommentByUser)", authorId, commentId);
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getCommentByUser(authorId, commentId));
    }

    @GetMapping("user/events/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> getAllCommentsByEvent(@NonNull @PathVariable("eventId") Long eventId) {
        log.info("Вызов метода получение всех комментариев к событию с ID = {} (getAllCommentsByEvent)", eventId);
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getAllCommentsByEvent(eventId));
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateCommentByUser(@NonNull @PathVariable("userId") Long authorId,
                                                          @NonNull @PathVariable("commentId") Long commentId,
                                                          @Valid @RequestBody CommentDto commentDto) {
        log.info("Вызов метода обновления комментария с ID = {} пользователем с ID = {} (updateCommentByUser)",
                authorId, commentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.updateCommentByUser(authorId, commentId, commentDto));
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@NonNull @PathVariable("userId") Long authorId,
                                    @NonNull @PathVariable("commentId") Long commentId) {
        log.info("Вызов метода удаления комментария с ID = {} автором с ID ={} (deleteCommentByUser)",
                commentId, authorId);
        commentService.deleteCommentByUser(authorId, commentId);
    }

    @PatchMapping("admin/comments/{commentId}")
    public ResponseEntity<CommentDto> updateCommentByAdmin(@NonNull @PathVariable("commentId") Long commentId,
                                                           @Valid @RequestBody CommentDto commentDto) {
        log.info("Вызов метода обновления комментария с ID = {} администратором (updateCommentByAdmin)",
                commentId);
        return ResponseEntity.status(HttpStatus.OK).body(commentService.updateCommentByAdmin(commentId, commentDto));
    }

    @DeleteMapping("admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@NonNull @PathVariable("commentId") Long commentId) {
        log.info("Вызов метода удаления комментария с ID = {} администратором (deleteCommentByAdmin)",
                commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping("events/{eventId}/comments")
    public ResponseEntity<List<CommentDtoPublic>> getAllCommentsByEventPublic(@NonNull @PathVariable("eventId") Long eventId) {
        log.info("Вызов метода получения всех комментариев к событию с ID = {} (getAllCommentsByEventPublic).", eventId);
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getAllCommentsByEventPublic(eventId));
    }
}
