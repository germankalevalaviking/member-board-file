package com.example.consult.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {

    // 수정 및 삭제할 댓글을 식별하기 위한 댓글 ID
    private Long commentId;

    @NotBlank(message = "댓글을 입력하세요")
    @Size(min = 10, max = 100, message = "댓글은 최소 10글자, 최대 100글자 입력하세요")
    private String content;
}
