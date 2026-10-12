package com.example.consult.service;

import com.example.consult.config.CustomUserDetails;
import com.example.consult.dto.MemberFormDto;
import com.example.consult.entity.Member;
import com.example.consult.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    // 회원 정보가 중복되는지 검증후 데이터를 저장하는 메소드
    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    // 입력한 이메일로 기존 회원 정보를 조회하고 회원이 존재한다면 예외를 발생시켜 에러 메세지를 띄우는 메소드
    private void validateDuplicateMember(Member member) {
        Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());
        if (findMember.isPresent()) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    // 사용자의 이메일을 통해 회원정보를 조회하여 비밀번호를 찾는 메서드
    // 일치하는 회원이 존재하지 않으면 예외를 발생시킴
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

    }

    // 이름과 전화번호를 기준으로 회원 정보를 조회하여 이메일을 찾는 메서드
    // 일치하는 회원이 존재하지 않으면 예외를 발생시킴
    public Member findEmail(String name, String phone) {
        return memberRepository.findByNameAndPhone(name, phone)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

    }


    // 이메일 비밀번호 찾기
    public Member findPassword(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }


    // 로그인한 회원의 이메일와 사용자가 입력한 회원정보를 담은 객체를 전달받음.
    public void updateMember(String loginMail, MemberFormDto memberFormDto) {
        // db에서 해당 이메일을 가진 회원정보를 조회
        Member member = memberRepository.findByEmail(loginMail)
                .orElseThrow(() -> // 회원정보가 없으면 에러 발생후 메서드 종료
                        new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 회원 정보 입력폼에 입력한 비밀번호가 null이 아니고, 회원 정보 입력폼에 입력한 비밀번호가 비어있지 않다면
        if (memberFormDto.getPassword() != null &&
                !memberFormDto.getPassword().isBlank()) {
            // 비밀번호를 입력하면 새 비밀번호로 변경하고, 입력하지 않으면 기존 비밀번호를 유지

            // 입력한 비밀번호를 암호화해서 세팅
            member.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        }
        // 이메일은 수정하지 않고 이름과 전화번호만 수정
        member.setName(memberFormDto.getName());
        member.setPhone(memberFormDto.getPhone());

        // 수정된 회원정보을 저장
        memberRepository.save(member);
    }

    // 조회 전용 트랜잭션으로 모든 회원 정보를 조회
    @Transactional(readOnly = true)
    public List<Member> list() {
        // memberRepository 의 findAll() 메소드를 호출해서 모든 회원정보를 가져옴
        return memberRepository.findAll();
    }

    // 회원의 id를 조회하여 회원의 상세 정보를 조회하는 메소드
    // 해당 회원이 존재하지 않을 경우 예외를 발생시킴
    @Transactional(readOnly = true)
    public Member detail(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public void deleteMember(String loginEmail) {
        Member member = findByEmail(loginEmail);
        memberRepository.delete(member);
    }

    // UserDetailsService 인터페이스의 loadUserByUsername() 메소드를 오버라이딩,
    // 로그인 할 유저의 email을 파라미터로 전달 받음
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
       /* Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다.")); */

        // 로그인 할 이메일로 회원을 조회
        Optional<Member> member = memberRepository.findByEmail(email);
        // 해당 회원이 없으면 로그인 인증 실패
        if (member.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }

        // Optional 안에 들어있는 Member를 꺼냄
        Member members = member.get();

//        return User.builder() // UserDetails를 구현하고 있는 User 객체를 반환
//                .username(member.getEmail())
//                .password(member.getPassword())
//                .roles(member.getRole().toString())
//                .build();

        // 롤(Role) 설정을 위한 권한 생성 (기본 User.builder가 처리하던 방식과 동일하게 맞춤)
        // 회원의 Role을 Spring Security에서 사용하는 권한으로 변환
        List<GrantedAuthority> authorities =
                AuthorityUtils.createAuthorityList("ROLE_" + members.getRole().toString());

        // 기본 User 대신 커스텀 CustomUserDetails 반환 (실제 이름 members.getName()을 넘겨줌)
        return new CustomUserDetails(
                members.getEmail(),
                members.getPassword(),
                authorities,
                members.getName() // 실제 이름 추가!
        );

        /* loadUserByUsername()은 Spring Security 로그인 과정에서 호출되는 메서드.
           이메일로 회원을 조회한 다음 회원의 이메일, 비밀번호, 권한 등의 정보를
           CustomUserDetails에 담아서 Spring Security에 반환 */

        // 기본 User 객체 대신 로그인한 회원의 실제 이름도 사용할 필요가 있어서 CustomUserDetails를 만들어 이름을 추가로 저장
    }
}
