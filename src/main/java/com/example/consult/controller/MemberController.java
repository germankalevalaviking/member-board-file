package com.example.consult.controller;

import com.example.consult.dto.MemberFormDto;
import com.example.consult.entity.Member;
import com.example.consult.service.MemberService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    // 회원 관련 비즈니스 로직을 처리하는 MemberService 클래스 객체를 주입 받음
    private final MemberService memberService;
    // 비밀번호 암호화에 사용하는 PasswordEncoder 인터페이스 객체를 주입 받음
    private final PasswordEncoder passwordEncoder;

     /*
         * 회원가입 폼을 보여주는 메서드.
         * /member/new로 GET 요청이 들어오면 빈 MemberFormDto 객체를 생성하여
         * Model에 담고 회원가입 화면으로 이동한다.
     */
    @GetMapping(value = "/new")
    public String memberForm(@Nonnull Model model) {  // Model 객체를 파라미터로 받음
        // 회원가입 입력 폼에서 사용할 빈 MemberFormDto 객체를 생성하여 Model에 담음
        model.addAttribute("memberFormDto", new MemberFormDto());
        // member폴더의 memberForm 화면으로 이동
        return "member/memberForm";
    }

    @GetMapping("/login") // /member/login으로 GET 요청이 들어오면
    // model 객체와 요청 파라미터 error를 전달받음. (required=false이므로 error 파라미터가 없어도 요청 가능)
    public String loginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) { // error가 null이 아닌 경우 (로그인 실패)
            // loginError라는 key와 이메일 또는 비밀번호가 올바르지 않습니다. 라는 value 를 model에 담아서
            model.addAttribute("loginError", "이메일 또는 비밀번호가 올바르지 않습니다.");
        } // member폴더의 loginForm 화면으로 이동
        return "member/loginForm";
    }

    @PostMapping("/new")  //  /member/new로 POST 요청(회원가입 양식 제출)이 들어오면
    public String newMember(@Valid MemberFormDto memberFormDto,
                            BindingResult bindingResult, Model model) {
        // DTO 필드 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            // 다시 입력 폼으로 이동
            return "member/memberForm";
        }

        try { // 회원가입 성공 시
            // DTO -> Entity 변환 (비밀번호 암호화 포함)
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member); //
        } catch (IllegalStateException e) { // 예외 처리.(회원가입 실패)
            model.addAttribute("errorMessage", e.getMessage());
            // 회원가입 입력 폼으로 다시 이동
            return "member/memberForm";
        }
        // 가입 성공 시 경우 로그인 화면으로 리다이렉트
        return "redirect:/member/login";
    }

    @GetMapping("/list")
    public String memberList(Model model) {
        // memberService의 list()를 호출해서 회원목록을 조회
        List<Member> memberList = memberService.list();
        // 회원목록 객체를 model에 담아서
        model.addAttribute("memberList", memberList);
        // member/memberList.html로 전달
        return "member/memberList";
    }

    @GetMapping("/detail/{id}")// // URL 경로에 포함된 회원 PK({id})를 GET 방식으로 요청받음
    // @PathVariable을 통해 URL 경로의 id 값을 추출하고, 화면에 데이터를 넘겨줄 Model 객체를 파라미터로 받음
    public String memberDetail(@PathVariable Long id, Model model) {
        // 전달받은 id로 회원 상세 정보를 조회하는 detail() 메서드를 호출하여 Member 객체를 반환받음
        Member member = memberService.detail(id);
        // 반환 받은 회원 객체를 model에 담고
        model.addAttribute("member", member);
        // 회원 상세 화면(member/memberDetail.html)으로 이동
        return "member/memberDetail";
    }

    @GetMapping("/memberUpdateForm")
    public String updateForm(Model model,  // Model 객체와
                             Principal principal) { // Principal를 객체를 파라미터로 받음

        // 현재 로그인한 사용자의 이메일을 가져옴
        String loginEmail = principal.getName();

        // 로그인한 사용자의 이메일을 기준으로 회원 정보를 조회
        Member member = memberService.findByEmail(loginEmail);

        // 회원정보 수정 폼에서 사용할 DTO 객체를 생성
        MemberFormDto memberFormDto = new MemberFormDto();

        // 조회한 기존 회원 정보를 DTO에 담아서 수정 폼에 표시
        memberFormDto.setEmail(member.getEmail());
        memberFormDto.setName(member.getName());
        memberFormDto.setPhone(member.getPhone());

        // 기존 회원정보가 담긴 DTO를 Model에 저장하고
        model.addAttribute("memberFormDto", memberFormDto);
        // 회원정보 수정 view를 리턴
        return "member/memberUpdateForm";
    }

    @PostMapping("/update")
    public String updateMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult,
                               Principal principal, Model model) {
        // 유효성 검증(@Valid)에 실패하면
        if (bindingResult.hasErrors()) {
            // 다시 수정 폼으로 이동
            return "member/memberUpdateForm";
        }
        try { // true
            // 현재 로그인한 사용자의 이메일(username)을 가져옴
            String loginEmail = principal.getName();
            // 서비스 단의 회원 수정 메서드 호출
            // (로그인한 사용자의 이메일과 수정 폼에 입력된 정보를 Service에 전달하여 회원정보 수정)
            memberService.updateMember(loginEmail, memberFormDto);
            // 수정에 성공하면 회원 목록 페이지로 리다이렉트
            return "redirect:/member/list";
        } catch (Exception e) { // false (예외 발생)
            // model에 에러 메시지를 담아서 view(화면)에 전달
            model.addAttribute("errorMessage", e.getMessage());
            // model에 회원정보 입력폼에 입력한 내용들을 담아서 view(화면)에 전달
            model.addAttribute("memberFormDto", memberFormDto);
            return "member/memberUpdateForm";
        }
//        return "redirect:/"; // 성공하면 메인 페이지로 이동
    }

    // 회원 삭제 처리
    @PostMapping("/delete")
    public String deleteMember(Principal principal, HttpServletRequest request, Model model) {
        try {
            // 1. 현재 로그인한 사용자의 이메일을 가져옴
            String loginEmail = principal.getName();
            // 2. Service의 회원 삭제 메서드를 호출하여 해당 회원을 DB에서 삭제
            memberService.deleteMember(loginEmail);
            // 3. 회원 삭제 후 현재 로그인 세션을 무효화하여 로그아웃 상태로 만듦
            request.getSession().invalidate();
            // 메인 페이지로 리다이렉트
            return "redirect:/";
        } catch (Exception e) {
            // 만에 하나 삭제 중 예외(DB에러 등) 발생 시 에러 메시지를 지참하여 상세 페이지로 백업
            model.addAttribute("errorMessage", "회원 탈퇴 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return "member/memberDetail";
        }
    }

    @GetMapping("/findEmail")
    public String findEmailForm() {
        return "member/findEmail";
    }

    @PostMapping("/findEmail")
    public String findEmail(@RequestParam("name") String name,
                            @RequestParam("phone") String phone) {
        // 사용자가 입력한 이름과 전화번호를 Service에 전달하여 회원 정보를 조회
        Member member = memberService.findEmail(name, phone);

        // 조회된 회원과 이메일이 존재하면
        if (member != null && member.getEmail() != null) {
            // 이메일 뒤의 4글자를 마스킹 처리 (예: hongsb****@naver.com)
            String rawEmail = member.getEmail(); // 회원의 원본 이메일을 가져옴
            String maskedEmail = maskEmail(rawEmail); // 개인정보 보호를 위해 이메일 일부를 마스킹 처리
            // 마스킹된 이메일을 결과 페이지로 전달
            return "redirect:/member/findEmailResult?email=" + maskedEmail;
        }
        // 일치하는 회원이 없으면 error=true를 전달
        return "redirect:/member/findEmailResult?error=true";
    }

    @GetMapping("/findEmailResult")
    public String findEmailResult(@RequestParam(value = "email", required = false) String email,
                                  @RequestParam(value = "error", required = false) String error,
                                  Model model) {
        if (error != null) { // error 요청 파라미터가 존재하면 회원 정보를 찾지 못한 것으로 처리
            // 결과 화면에 출력할 안내 메시지를 Model에 저장
            model.addAttribute("email", "일치하는 회원 정보가 없습니다.");
        } else { // 에러가 아닌 경우
            // 조회된 이메일을 결과 화면에 전달
            model.addAttribute("email", email);
        }
        // 이메일 찾기 결과 화면으로 이동
        return "member/findEmailResult";
    }

    @GetMapping("/findPassword")
    public String findPasswordForm() {
        // 이메일 찾기 입력 화면을 보여줌
        return "member/findPassword";
    }

    @PostMapping("/findPassword")
    public String findPassword(@RequestParam("email") String email) {
        // 사용자가 입력한 이메일을 Service에 전달하여 비밀번호를 조회
        Member member = memberService.findPassword(email);

        if (member != null) {
            // 실무 스타일: 비밀번호를 보여주지 않고 변경 페이지로 이동시키거나 임시 비밀번호 안내
            // 포트폴리오용으로 안전하게 "인증 성공 및 변경 안내" 메시지만 전달
            return "redirect:/member/findPasswordResult?success=true";
        }

        return "redirect:/member/findPasswordResult?error=true";
    }

    @GetMapping("/findPasswordResult")
    public String findPasswordResult(@RequestParam(value = "success", required = false) String success,
                                     @RequestParam(value = "error", required = false) String error,
                                     Model model) {
        if (error != null) {
            model.addAttribute("message", "일치하는 회원 정보가 없습니다.");
        } else if (success != null) {
            model.addAttribute("message", "회원님의 이메일로 비밀번호 재설정 링크가 발송되었습니다. (또는 임시 비밀번호가 발급되었습니다.)");
        }
        return "member/findPasswordResult";
    }

    // 이메일 혹은 비밀번호 찾기 시 개인정보 보호를 위해
    // 이메일의 일부만 보여주고 나머지는 ***로 마스킹하는 메서드
    private String maskEmail(String email) {
        try {
            // @를 기준으로 이메일을 분리
            // 예) test123@gmail.com
            // -> parts[0] = "test123"
            // -> parts[1] = "gmail.com"
            String[] parts = email.split("@");

            // 이메일 아이디 부분 저장 -> test123
            String id = parts[0];

            // 이메일 도메인 부분 저장 -> gmail.com
            String domain = parts[1];

            // 아이디 길이가 3글자 이하라면
            if (id.length() <= 3) {
                // 첫 글자만 보여주고 나머지는 ** 로 마스킹
                // abc@gmail.com -> a**@gmail.com
                return id.substring(0, 1) + "**@" + domain;
            } else {
                // 아이디 길이가 4글자 이상이라면 마지막 3글자를 ***으로 마스킹
                // test123@gmail.com -> test***@gmail.com
                return id.substring(0, id.length() - 3) + "***@" + domain;
            }
        } catch (Exception e) {
            // 이메일 형식이 잘못되었거나
            // split(), substring() 등에서 예외가 발생하면
            // 원본 이메일을 그대로 반환
            return email; // (에러 발생 시 예외 처리)
        }
        /**
         * "이메일 또는 비밀번호 찾기 기능에서는 개인정보 보호를 위해 이메일 전체를 노출하지 않고 일부만 공개하도록 구현했습니다.
         * substring()을 사용해 아이디 앞부분만 보여주고 나머지는 ***로 마스킹했으며,
         * 이메일 형식이 올바르지 않아 예외가 발생하는 경우에는 try-catch를 통해 예외를 처리하도록 작성했습니다."
         */

        /**
         * Q. 왜 이메일 마스킹 기능을 구현했나요?
         * "회원의 이메일이나 비밀번호 찾기 기능에서 개인정보를 그대로 노출하는 것은 보안상 좋지 않다고 생각했습니다.
         * 그래서 이메일의 일부만 보여주고 나머지는 ***로 마스킹하여 개인정보를 보호하도록 구현했습니다."
         * Q. 어떻게 구현했나요?
         * "split()으로 이메일을 아이디와 도메인으로 분리한 뒤, substring()을 이용해 아이디 앞부분만 가져왔습니다.
         * 아이디 길이가 짧은 경우와 긴 경우를 if문으로 나누어 각각 ** 또는 ***를 붙여 마스킹했습니다.
         * 또한 잘못된 이메일 형식으로 예외가 발생할 수 있으므로 try-catch를 사용해 예외 처리도 했습니다."
         */
    }
}
