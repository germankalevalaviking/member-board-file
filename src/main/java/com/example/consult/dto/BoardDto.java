package com.example.consult.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class BoardDto {

    @NotEmpty(message = "제목을 입력하세요.")
    @Size(min=4, max=20, message = "제목은 4자 이상 20자 이하로 입력하세요.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    @Size(min=10, max=100, message = "내용은 10자 이상 100자 이하로 입력하세요.")
    private String content;

    // 파일 업로드용 필드 (클라이언트에서 파일 데이터를 전달받기 위해 MultipartFile 필드를 추가)
    // 사용자가 선택한 실제 파일 데이터를 임시로 받음
    private MultipartFile file;
}
