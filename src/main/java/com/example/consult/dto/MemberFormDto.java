package com.example.consult.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
public class MemberFormDto {
    // 회원가입 화면에서 입력받는 데이터를 담고, 입력값의 유효성을 검증하는 DTO

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식으로 입력하세요.")
    private String email;

    @NotEmpty(message = "사용자 이름을 입력하세요.")
    private String name;

    @NotEmpty(message = "전화번호를 입력하세요.")
    private String phone;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min=8, max=15, message = "비밀번호는 8자 이상, 15자 이하로 입력해주세요.")
    private String password;
}
