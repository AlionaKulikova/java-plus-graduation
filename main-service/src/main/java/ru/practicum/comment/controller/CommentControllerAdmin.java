package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDtoResponse;
import ru.practicum.comment.service.CommentService;

@RestController
@Slf4j
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentControllerAdmin {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDtoResponse> getComment(@PathVariable Long commentId) {
        log.info("Эндпоинт /admin/comments/{}. GET запрос по на получение комментария по id {}.", commentId, commentId);
        return new ResponseEntity<>(commentService.getCommentAdmin(commentId), HttpStatus.OK);
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteCommentAdmin(commentId);
        log.info("Эндпоинт /admin/comments/{}. DELETE запрос  на удаление комментария id {}.", commentId,
                commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
