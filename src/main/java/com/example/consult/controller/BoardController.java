package com.example.consult.controller;

import com.example.consult.dto.BoardDto;
import com.example.consult.dto.CommentDto;
import com.example.consult.entity.Board;
import com.example.consult.entity.Comment;
import com.example.consult.entity.Member;
import com.example.consult.service.BoardService;
import com.example.consult.service.CommentService;
import com.example.consult.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController { // 일반 회원용 Controller

    private final BoardService boardService;

    private final CommentService commentService;

    private final MemberService memberService;

    @Value("${file.dir}")
    private String fileDir;

    // 게시글 작성 화면 요청
    @GetMapping("/new")
    public String boardForm(Model model, Principal principal) { // 사용자 정보 입력 폼
        if(principal == null) { // 현재 인증된 사용자가 없는 경우
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        // 현재 로그안한 사용자의 이메일을 이용하여 조회한 결과를 loginMember에 할당
        Member loginMember = memberService.findByEmail(principal.getName());
        // 작성 화면에서 로그인 한 회원 정보를 사용할 수 있도록 model에 저장
        model.addAttribute("loginMember", loginMember);
        // 게시글 작성 폼에서 사용할 빈 BoardDto 객체를 Model에 전달
        model.addAttribute("boardDto", new BoardDto());
        return "board/boardForm";
    }

    // 게시글 저장
    @PostMapping("/new")
    public String createBoard(@Valid BoardDto boardDto, BindingResult bindingResult,
                              Model model, Principal principal) throws IOException { // 예외처리 추가
        // 로그인 하지 않았다면
        if (principal == null) {
            // 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        // 로그인한 회원정보 조회
        Member loginMember = memberService.findByEmail(principal.getName());
        // 조회한 회원정보를 모델에 담음
        model.addAttribute("loginMember", loginMember);

        // DTO 검증 실패 시
        if (bindingResult.hasErrors()) {
            return "board/boardForm"; // 다시 작성 화면으로 이동
        }
        String loginEmail = principal.getName(); // 로그인한 회원정보를 loginEmail 변수에 할당
        boardService.saveBoard(boardDto, loginEmail); // 파일 정보가 포함된 DTO 전달
        return "redirect:/board/complete"; // 등록 완료 페이지로 이동
    }

    // 첨부파일 다운로드 메서드
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) throws MalformedURLException {
        // boardService를 통해 전달받은 id(PK)에 해당하는 게시글 엔티티(Board)를 DB에서 조회
        Board board = boardService.findById(id);
        // 서버에 실제로 저장된 파일명을 가져옴 (예: 550e8400-e29b-41d4-a716-446655440000.pdf)
        String storedFileName = board.getStoredFileName();
        // 사용자가 처음 업로드했던 원본 파일명을 가져옴 (예: 이력서.pdf)
        String originalFileName = board.getOriginalFileName();

        // 서버의 저장된 실제 파일을 Resource 객체로 생성
        UrlResource resource = new UrlResource("file:" + fileDir + storedFileName);

        // 한글이나 공백이 포함된 원본 파일명이 깨지지 않도록
        // UTF-8 방식으로 인코딩함
        String encodedOriginalFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedOriginalFileName + "\"";

        // HTTP 응답 객체를 생성하고 반환
        return ResponseEntity.ok() // HTTP 상태 코드 200 OK 설정
                // Content-Disposition 헤더 설정 (다운로드 동작 지정)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                // 응답 본문(body)에 실제 파일 데이터(Resource)를 실어서 전달
                .body(resource);
    }

    // 게시글 작성 완료 화면 출력
    @GetMapping("/complete")
    public String completeForm() {
        return "board/boardComplete";
    }


    // 관리자/일반 사용자 상세 보기 (조회수 쿠키 로직 적용)
    @GetMapping("/detail/{id}")
    public String boardDetail(@PathVariable("id") Long id, Model model,
                              HttpServletRequest request, HttpServletResponse response) {
        // ---- [쿠키를 이용한 조회수 중복 방지 로직 시작] ----

        // 조회수 중복 방지 쿠키 확인(Cookie ~ (oldCookie);)
        Cookie oldCookie = null; // 기존에 저장되어 있는 boardView 쿠키를 담기 위한 변수 (처음에는 없으므로 null)
        Cookie[] cookies = request.getCookies(); // 사용자가 현재 가지고 있는 모든 쿠키를 가져옴.

        // 기존 boardView 쿠키가 있을 때(if ~ break;)
        if (cookies != null) { // 사용자의 브라우저에 쿠키가 존재한다면
            for (Cookie cookie : cookies) { // 모든 쿠키를 하나씩 확인
                if (cookie.getName().equals("boardView")) { // 쿠키 이름이 boardView인지 확인
                    oldCookie = cookie; // 찾은 boardView 쿠키를 oldCookie 변수에 저장
                    break;}}} // 더 이상 찾을 필요가 없으므로 반복문 종료

        // boardView 쿠키가 이미 존재하는 경우 (기존 boardView 쿠키가 있을 때)
        if (oldCookie != null) {
            // 현재 게시글 번호(id)가 쿠키 안에 저장되어 있는지 확인
            /* 예) * 쿠키값 : [1]_[3]_[8]
             * 현재 id : 5
             * -> 포함되어 있지 않으면 조회수를 증가시킨다. */
            if (!oldCookie.getValue().contains("[" + id + "]")) {
                boardService.increaseView(id); // 조회수 1 증가
                // 현재 게시글 번호를 쿠키 뒤에 추가
                /* 기존 : [1]_[3]
                   변경 : [1]_[3]_[5] */
                oldCookie.setValue(oldCookie.getValue() + "_[" + id + "]");
                oldCookie.setPath("/"); // 사이트 전체에서 사용할 수 있도록 쿠키 경로 지정
                oldCookie.setMaxAge(60 * 60 * 24);  // 쿠키 유지시간을 24시간으로 설정 (24시간이 지나면 자동 삭제)
                response.addCookie(oldCookie); // 수정된 쿠키를 다시 브라우저에 저장
            }
        } else { // boardView 쿠키 자체가 없는 경우 (boardView 쿠키가 없을 때)
            // 처음 조회하는 게시글이므로 조회수 증가
            boardService.increaseView(id);
            // 새로운 boardView 쿠키 생성, 현재 게시글 번호를 저장
            Cookie newCookie = new Cookie("boardView", "[" + id + "]");
            // 사이트 전체에서 사용할 수 있도록 설정
            newCookie.setPath("/");
            // 쿠키 유지시간 24시간
            newCookie.setMaxAge(60 * 60 * 24);
            // 새 쿠키를 브라우저에 저장
            response.addCookie(newCookie);
        }
        // ---- [쿠키를 이용한 조회수 중복 방지 로직 끝] ----

        // 해당 게시글의 id 조회
        Board board = boardService.findById(id);
        // 게시글에 달린 댓글 목록 조회
        List<Comment> commentList = commentService.getListByBoard(board);

        // 해당 게시글과 댓글들을 한번에 조회
        model.addAttribute("board", board);
        model.addAttribute("commentList", commentList);
        model.addAttribute("commentDto", new CommentDto());
        return "board/boardDetail";
    }

    // 수정 폼 이동
    @GetMapping("/{id}/update")
    public String updateForm(@PathVariable("id") Long id, Model model) {
        // id에 해당하는 수정할 게시글을 DB에서 조회해서 board 변수에 저장.(수정할 글 정보를 가져와라)
        Board board = boardService.detail(id);

        // 빈 DTO 객체를 하나 생성.(수정 화면에 넘겨줄 DTO를 만들어라)
        BoardDto boardDto = new BoardDto();
        // 조회한 게시글의 제목과 내용을 수정 폼에 표시하기 위해 DTO에 설정
        boardDto.setTitle(board.getTitle());
        boardDto.setContent(board.getContent());

        // DTO를 View로 전달
        model.addAttribute("boardDto", boardDto);
        // 현재 수정 중인 번호(id)를 View로 전달
        model.addAttribute("boardId", id);
        return "board/boardUpdateForm";
    }

    // 수정 처리
    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Long id, Model model,
                         @Valid BoardDto boardDto,
                         BindingResult bindingResult) throws IOException{

        // 입력값 검증에 실패하면
        if(bindingResult.hasErrors()) {
            // 게시글 ID를 model에 넣고
            model.addAttribute("boardId", id);
            // 수정화면으로 이동
            return "board/boardUpdateForm";
        }
        // 게시글 ID와 수정 데이터를 Service에 전달하여 수정 처리
        boardService.update(id, boardDto);
        // 수정 후 해당 id 게시글의 상세 화면으로 리턴
        return "redirect:/board/detail/" + id;
    }

    // 게시글 삭제 기능
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        // 실제 삭제 작업은 Controller가 직접 하는 게 아니라 BoardService.delete(id)에 위임
        boardService.delete(id); // 해당 id를 가진 게시글을 삭제
        return "redirect:/board/list"; // 삭제가 반영된 list 화면을 보주여줌
    }

    @GetMapping("/list")
    public String list(Model model, Principal principal,
                       @RequestParam(value = "page", defaultValue = "0") int page, // page 파라미터 (없으면 기본값 0 → 첫 페이지)
                       @RequestParam(value = "search", defaultValue = "") String search) { // search 파라미터 (검색 키워드, 없으면 빈 문자열)
        // 로그인한 회원 이름을 header에서 사용하기 위해 전달
        if(principal != null) {
            // 이메일을 통해 사용자 정보를 조회하고
            Member loginMember = memberService.findByEmail(principal.getName());
            // 조회한 사용자 정보를 model에 저장
            model.addAttribute("loginMember", loginMember);
        }
        // Specification<Board>: 동적으로 검색 조건을 정의하는 부분
        Specification<Board> spec = (root, query, cb) -> {

            // 검색어가 없거나 스페이스(공백)만 입력된 경우 검색 조건 무시
            if (search == null || search.trim().isEmpty()) {
                return null;
            }
            // 검색어가 있으면: 제목(boardTitle)이나 내용(boardContent)에 포함된 게시글만 조회(false)
            return cb.or(
                    cb.like(root.get("title"), "%" + search + "%"), // title에 검색어가 포함되어 있는가?
                    cb.like(root.get("content"), "%" + search + "%") // content에 검색어가 포함되어 있는가?
            );
        };
        // BoardService 호출 → (페이징 + 검색 조건) 적용된 게시글 목록 가져오기
        Page<Board> paging = boardService.getList(page, spec);

        // 뷰(템플릿)에서 사용할 수 있도록 모델에 데이터 담기
        model.addAttribute("paging", paging); // 페이징된 게시글
        model.addAttribute("search", search); // 검색어 (검색창에 다시 표시하기 위함)
        return "board/boardList";
    }
}
