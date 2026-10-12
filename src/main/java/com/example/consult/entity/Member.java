package com.example.consult.entity;

import com.example.consult.Role;
import com.example.consult.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "member")
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime regTime;

    // 회원가입 폼 DTO(MemberFormDto)의 데이터를 이용해서 Member 엔티티를 생성하는 정적 팩토리 메서드
    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder) {
        // 새로운 회원 엔티티 생성(member 인스턴스(객체)를 새로 만듬)
        Member member = new Member();

        // DTO -> Entity
        member.setEmail(memberFormDto.getEmail());
        member.setName(memberFormDto.getName());
        member.setPhone(memberFormDto.getPhone());

        // 입력한 비밀번호를 암호화해서 세팅
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);

        // 신규 회원의 권한과 가입 시간 설정
        member.setRole(Role.USER);
        member.setRegTime(LocalDateTime.now());

        return member; // 생성과 초기 설정이 완료된 Member 엔티티 반환
    }
   /* createMember()는 회원가입 DTO를 Member 엔티티로 변환하면서
    비밀번호를 암호화하고 기본 권한, 가입 시간을 설정하는 정적 팩토리 메서드.
    static으로 선언해서 Member 객체를 미리 생성하지 않고 Member.createMember() 형태로 호출할 수 있도록 함. */


}
