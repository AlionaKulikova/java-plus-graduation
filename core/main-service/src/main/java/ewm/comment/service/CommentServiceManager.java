package ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ewm.comment.dto.CommentDtoRequest;
import ewm.comment.dto.CommentDtoResponse;
import ewm.comment.mapper.CommentMapper;
import ewm.comment.model.Comment;
import ewm.comment.repository.CommentRepository;
import ewm.error.exception.ConflictException;
import ewm.error.exception.ResourceNotFoundException;
import ewm.events.model.Event;
import ewm.events.model.EventState;
import ewm.events.repository.EventRepository;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceManager implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDtoResponse getCommentAdmin(Long commentId) {
        CommentDtoResponse responseCommentDto = commentMapper.toResponseCommentDto(
                commentRepository.findById(commentId)
                        .orElseThrow(() -> new ResourceNotFoundException(Comment.class, commentId)));

        log.info("Получение администратором комментария c id {}.", commentId);
        return responseCommentDto;
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException(Comment.class, commentId);
        }
        commentRepository.deleteById(commentId);
        log.info("Удаление администратором комментария c id {}.", commentId);
    }

    @Override
    public List<CommentDtoResponse> getCommentsOfEventPublic(Long eventId, Integer from, Integer size) {
        List<CommentDtoResponse> responseCommentsDTO = commentMapper.toResponseCommentDto(commentRepository
                .findAllCommentsForEvent(eventId, from, size));

        log.info("Получение всех комментариев для события c id {} по параметрам:  from: {}, size: {}.",
                eventId, from, size);
        return responseCommentsDTO;
    }

    @Override
    public List<CommentDtoResponse> getAllCommentsByUserIdPrivate(Long userId) {
        List<CommentDtoResponse> responseCommentsDTO = commentMapper.toResponseCommentDto(
                commentRepository.getCommentsByAuthorId(userId));

        log.info("Получение комментариев вользователя с id {}.", userId);
        return responseCommentsDTO;
    }

    @Override
    public CommentDtoResponse getCommentByIdByUserIdPrivate(Long userId, Long commentId) {

        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(Comment.class, commentId));
        if (!foundComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Данный пользователь не является автором получаемого комментария.");
        }
        CommentDtoResponse responseCommentDto = commentMapper.toResponseCommentDto(foundComment);

        log.info("Получение комментария c id {} пользователя c id {}.", commentId, userId);
        return responseCommentDto;
    }

    @Override
    @Transactional
    public CommentDtoResponse createCommentPrivate(CommentDtoRequest newComment, Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(Event.class, eventId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, userId));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Комментируемое событие не опубликовано.");
        }
        Comment comment = commentRepository.save(commentMapper.commentDtotoComment(newComment, user, event));
        CommentDtoResponse responseCommentDto = commentMapper.toResponseCommentDto(comment);

        log.info("Создание комментария {} пользователем с  id {} к событию c id {}.", newComment, userId, eventId);
        return responseCommentDto;
    }

    @Override
    @Transactional
    public CommentDtoResponse updateCommentPrivate(CommentDtoRequest newComment, Long userId, Long commentId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(User.class, userId);
        }
        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(Comment.class, commentId));
        if (!foundComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Данный пользователь не является автором обновляемого комментария.");
        }
        foundComment.setMessage(newComment.getMessage());
        CommentDtoResponse commentDtoResponse = commentMapper.toResponseCommentDto(commentRepository.save(foundComment));

        log.info("Обновление комментария c id {} пользователем c id {} на новый комментарий {}.", commentId,
                userId, newComment);
        return commentDtoResponse;
    }

    @Override
    @Transactional
    public void deleteByIdByUser(Long userId, Long commentId) {
        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(Comment.class, commentId));
        if (!foundComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Данный пользователь не является автором удаляемого комментария.");
        }
        commentRepository.deleteById(commentId);
        log.info("Удаление комментария c id {} пользователем c id {}.", commentId, userId);
    }
}