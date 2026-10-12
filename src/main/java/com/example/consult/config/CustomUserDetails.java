package com.example.consult.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

// Spring Security의 User를 상속받아 실제 이름(name)을 보관할 수 있는 커스텀 클래스
public class CustomUserDetails extends User {

    private final String name; // 실제 이름을 담을 필드 추가

    public CustomUserDetails(String username, String password,
                     Collection<? extends GrantedAuthority> authorities, String name) {
        super(username, password, authorities);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
