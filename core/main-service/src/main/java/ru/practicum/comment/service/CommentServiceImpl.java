package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoPublic;
import ru.practicum.comment.dto.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.exception.DataConflictRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final EventService eventService;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto addCommentToEvent(Long authorId, Long eventId, CommentDto commentDto) {
        Comment comment = commentMapper.toComment(commentDto);
        Event event = eventService.getEventById(eventId);
        User author = userService.findUserById(authorId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            comment.setAuthor(author);
            comment.setEvent(event);
            comment.setCreate(LocalDateTime.now());
            log.info("Добавлен новый комментарий к событию ID = {} пользователем c ID = {}.", eventId, authorId);
            return commentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            log.error("Добавление комментария невозможно, событие ID = {} не опубликовано.", eventId);
            throw new DataConflictRequest("The event has not been published yet.");
        }
    }

    @Override
    public CommentDto getCommentByUser(Long authorId, Long commentId) {
        log.info("Получение комментария с ID = {} для просмотра пользователем с ID = {}.", commentId, authorId);
        return commentMapper.toCommentDto(getCommentById(commentId));
    }

    @Override
    public List<CommentDto> getAllCommentsByEvent(Long eventId) {
        Event event = eventService.getEventById(eventId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            List<Comment> comments = commentRepository.findAllByEventOrderByEvent(event);
            log.info("Получение комментариев к событию с ID = {}.", eventId);
            return comments.stream().map(commentMapper::toCommentDto).collect(Collectors.toList());
        } else {
            log.error("Комментирование события с ID = {} пока не доступно, оно не опубликовано.", eventId);
            throw new DataConflictRequest("The event has not been published yet. Commenting is not available.");
        }
    }

    @Override
    public CommentDto updateCommentByUser(Long authorId, Long commentId, CommentDto commentDto) {
        Comment comment = getCommentById(commentId);
        User author = userService.findUserById(authorId);
        if (comment.getAuthor().getId().equals(author.getId())) {
            comment.setText(commentDto.getText());
            comment.setCreate(LocalDateTime.now());
            log.info("Комментарий к событию ID = {} обновлен автором.", commentId);
            return commentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            log.error("Изменение комментария ID = {} невозможно, пользователь не является автором.", commentId);
            throw new DataConflictRequest("The user is not the author of the comment.");
        }
    }

    @Override
    public void deleteCommentByUser(Long authorId, Long commentId) {
        Comment comment = getCommentById(commentId);
        if (comment.getAuthor().getId().equals(authorId)) {
            log.info("Комментарий с ID = {}, успешно удален автором комментария с ID = {}.", commentId, authorId);
            commentRepository.deleteById(getCommentById(commentId).getId());
        } else {
            throw new DataConflictRequest("The user is not the author of the comment.");
        }
    }

    @Override
    public CommentDto updateCommentByAdmin(Long commentId, CommentDto commentDto) {
        Comment comment = getCommentById(commentId);
        comment.setText(commentDto.getText());
        comment.setCreate(LocalDateTime.now());
        log.info("Комментарий к событию ID = {} откорректирован администратором.", commentId);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Комментарий с ID = {}, успешно удален администратором.", commentId);
        commentRepository.deleteById(getCommentById(commentId).getId());
    }

    @Override
    public List<CommentDtoPublic> getAllCommentsByEventPublic(Long eventId) {
        Event event = eventService.getEventById(eventId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            List<Comment> comments = commentRepository.findAllByEventOrderByEvent(event);
            log.info("Получение комментариев к событию с ID = {}.", eventId);
            return comments.stream().map(commentMapper::toCommentDtoPublic).collect(Collectors.toList());
        } else {
            log.error("Комментирование события с ID = {} пока не доступно, оно не опубликовано.", eventId);
            throw new DataConflictRequest("The event has not been published yet. Commenting is not available.");
        }
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment с ID = " + commentId + " не найден."));
    }
}
