package com.example.consult.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto { // 요청을 받을 때와 화면에 계층형으로 보여줄 때 사용하는 DTO

    private String content;

    private Long parentId; // 대댓글인 때만 값이 존재 (일반 댓글은 null)
}
