package com.example.consult.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

// Spring Security의 User를 상속받아 실제 이름(name)을 보관할 수 있는 커스텀 클래스
public class CustomUserDetails extends User {

    private final String name; // 실제 이름을 담을 필드 추가

    // CustomUserDetails 객체 생성 시
    // 로그인 아이디(username), 비밀번호(password), 권한(authorities), 사용자 이름(name)을 파라미터로 전달받음
    public CustomUserDetails(String username, String password,
                     Collection<? extends GrantedAuthority> authorities, String name) {
        // 부모 클래스(User)에 로그인 아이디, 비밀번호, 권한 정보를 전달
        super(username, password, authorities);
        // 추가로 사용할 사용자 이름을 CustomUserDetails에 저장
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
