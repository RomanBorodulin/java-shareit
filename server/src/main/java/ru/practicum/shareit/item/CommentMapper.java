package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    private CommentMapper() {

    }

    public static Comment toComment(CommentRequestDto commentDto, User user, Item item) {
        return commentDto != null ? Comment.builder()
                .text(commentDto.getText())
                .created(commentDto.getCreated())
                .author(user)
                .item(item)
                .build() : null;
    }

    public static CommentResponseDto toCommentDto(Comment comment) {
        return comment != null ? CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build() : null;
    }

    public static List<CommentResponseDto> toCommentDtoList(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

}
