package ru.practicum.comment.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommentDtoPublic {
    private Long id;
    private String text;
    private String eventId;
    private String create;
}
