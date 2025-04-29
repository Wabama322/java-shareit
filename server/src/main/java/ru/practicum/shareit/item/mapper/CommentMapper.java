package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CommentMapper {
    public Comment toComment(CommentDtoRequest commentDtoRequest, Item item, User author) {
        Comment comment = Comment.builder()
                .text(commentDtoRequest.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        return comment;
    }

    public List<CommentDtoResponse> toCommentDtoList(List<Comment> commentList) {
        return commentList.stream()
                .map(CommentMapper::toCommentDtoResponse)
                .collect(Collectors.toList());
    }

    public CommentDtoResponse toCommentDtoResponse(Comment comment) {
        return new CommentDtoResponse(comment.getId(), comment.getText(),
                comment.getAuthor().getName(), comment.getCreated());
    }
}
