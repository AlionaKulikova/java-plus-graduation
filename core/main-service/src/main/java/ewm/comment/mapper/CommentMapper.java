package ewm.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.comment.dto.CommentDtoRequest;
import ewm.comment.dto.CommentDtoResponse;
import ewm.comment.model.Comment;
import ewm.events.model.Event;
import ewm.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "author", source = "user")
    @Mapping(target = "id", ignore = true)
    Comment commentDtotoComment(CommentDtoRequest commentDtoRequest, User user, Event event);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "eventId", source = "event.id")
    CommentDtoResponse toResponseCommentDto(Comment comment);

    List<CommentDtoResponse> toResponseCommentDto(List<Comment> comments);
}
