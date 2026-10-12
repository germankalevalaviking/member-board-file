package com.example.consult.dto;

import com.example.consult.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CommentResponseDto {

    private Long id;

    private String content;

    private List<CommentResponseDto> children = new ArrayList<>();

    public CommentResponseDto(Comment comment) {
        this.id = Long.valueOf(comment.getId());
        this.content = comment.getContent();
        // 자식 엔티티들을 DTO로 변환하여 리스트에 담음
        this.children = comment.getChildren().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}
