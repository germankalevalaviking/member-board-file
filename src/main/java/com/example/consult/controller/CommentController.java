package com.example.consult.controller;

import com.example.consult.dto.CommentDto;
import com.example.consult.entity.Board;
import com.example.consult.entity.Comment;
import com.example.consult.entity.Member;
import com.example.consult.repository.CommentRepository;
import com.example.consult.service.BoardService;
import com.example.consult.service.CommentService;
import com.example.consult.service.MemberService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;

    private final CommentService commentService;

    private final BoardService boardService;

    private final MemberService memberService;


    // 원 댓글 작성 폼 화면 요청
    @GetMapping("/save/board/{id}")
    public String saveBoardCommentForm(@PathVariable Long id, @Nonnull Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board); // 뷰에서 ${board.id} 사용
        return "comment/boardDetail"; // 입력 화면 폼으로 리턴
    }

    // 원댓글 등록 초리
    @PostMapping("/save/board/{id}")
    public String saveBoardComment(@PathVariable("id") Long id, @Valid CommentDto commentDto,
                                   BindingResult bindingResult, Model model,
                                   Principal principal, RedirectAttributes attributes) {
        // 로그인 여부 확인
        if (principal == null) {
            attributes.addAttribute("error", "로그인 후 이용 가능합니다.");
            return "redirect:/member/login";
        }

        // 로그인 한 회원 정보를 가져옴
        Member loginMember = memberService.findByEmail(principal.getName());

        // 로그인을 한 사용자가 아닌 경우
        if (loginMember == null) {
            // 에러 메세지 출력 하고 로그인 페이지로 리다이렉트.
            attributes.addAttribute("error", "로그인 후 이용 가능합니다.");
            return "redirect:/member/login";
        }

        // boardService의 findById()로 게시글을 조회하고
        Board board = boardService.findById(id);

        // 검증 오류 발생 시 (10글자 미만이거나 100글자 초과일 때)
        if (bindingResult.hasErrors()) {
            // 화면에 다시 board 정보 전달
            model.addAttribute("board", board);
            model.addAttribute("commentList", commentService.getComments(board));
            // 입력 폼 화면으로 돌아가서 에러 메시지(BindingResult)를 타임리프 화면에 뿌림.
            return "board/boardDetail";
        }

        /* 어떤 게시글에 달린 댓글인가? → board
           누가 작성했는가? → loginMember
            댓글 내용은 무엇인가? → content
            를 서비스로 넘겨서 댓글을 생성*/
        // 3. 서비스 호출시 DTO의 값을 꺼내서 전달
        commentService.createComment(board, loginMember, commentDto.getContent());
        attributes.addFlashAttribute("message", "댓글 등록 완료");
        // redirect:/board/detail/3
        return "redirect:/board/boardDetail/" + board.getId();
    }

    @PostMapping("/reply/{parentId}")
    public String replyComment(@PathVariable("parentId") Integer parentId,
                               @RequestParam("boardId") Long boardId,
                               @RequestParam("content") String content,
                               Principal principal, RedirectAttributes attributes) {
        // 로그인한 회원인지 검증
        if (principal == null) {
            attributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/member/loginForm";
        }

        // boardId를 통해 어떤 게시글인지 조회하기 위한 메소드 호출
        Board board = boardService.findById(boardId);
        // 로그인한 회원정보를 가져옴(조회)
        Member loginMember = memberService.findByEmail(principal.getName());

        // 게시글(Board), 로그인한 회원(Member), 부모 댓글 번호(parentId), 대댓글 내용(content)을 전달하여 대댓글을 생성
        commentService.createReply(board, loginMember, parentId, content);
        attributes.addFlashAttribute("message", "대댓글이 등록되었습니다.");
        return "redirect:/board/detail/" + board.getId(); // ex) /board/detail/1

        // parentId : 어떤 댓글에 다는 대댓글인지(부모 댓글 번호)
        // boardId  : 어떤 게시글에 속한 댓글인지(게시글 번호)
        // content  : 사용자가 입력한 대댓글 내용
    }

    @GetMapping("/update/{id}")
    public String commentUpdateForm(@PathVariable(value = "id") Integer id, Model model) {
        Comment updated = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글은 존재하지 않습니다."));

        //화면에서 수정가능하도록 기존 content 데이터를 DTO에 담아서 보내면 더 좋음.
        CommentDto commentDto = new CommentDto();

        // Comment 엔티티의 ID 타입에 맞게 변환
        commentDto.setCommentId(Long.valueOf(updated.getId()));
        commentDto.setContent(updated.getContent());

        model.addAttribute("comment", updated);
        model.addAttribute("commentDto", commentDto);

        return "comment/commentUpdateForm";
    }

    @PostMapping("/update/{id}")
    public String updateComment(@PathVariable("id") Integer id, Model model, Principal principal,
                                @Valid CommentDto commentDto, BindingResult bindingResult,
                                RedirectAttributes attributes) {
        if (principal == null) { // 로그인 여부 확인
            // 에러 메세지 출력
            attributes.addFlashAttribute("error", "로그인 후 이용 가능합니다.");
            // loginForm으로 되돌림
            return "redirect:/member/loginForm";
        }

        // db에서 댓글 번호 조회해서 조회한 결과 값을 comment 에 할당.
        Comment comment = commentRepository.findById(id)
                // 예외 처리
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 로그인한 회원 정보를 가져와서
        Member loginMember = memberService.findByEmail(principal.getName());

        // 로그인 회원이 아니거나 댓글 작성자 아이디와 로그인한 회원 아이디가 일치하지 않을 경우
        if (loginMember == null || !comment.getMember().getId().equals(loginMember.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다."); // 예외를 터뜨림
        }

        if (bindingResult.hasErrors()) { // CommentDto의 유효성 검증에 실패 하면
            // model에 기존 댓글과 게시글 정보를 각각 담아서
            model.addAttribute("comment", comment);
            model.addAttribute("board", comment.getBoard());
            return "comment/commentUpdateForm"; // 다시 입력하도록 댓글 입력창으로 되돌림
        }
        // 검증을 통과하면 Service에 댓글 id와 수정할 내용을 전달하여 댓글 수정
        commentService.updateComment(id, commentDto.getContent());

        // 수정 환료 메세지를 리다이렉트 후 한번 만 사용할 수 있도록 저장
        attributes.addFlashAttribute("message", "댓글이 수정되었습니다.");

        // 댓글 수정 완료 후 해당 댓글이 속한 게시글 상세 화면으로 이동
        return "redirect:/board/detail/" + comment.getBoard().getId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        Long boardId = comment.getBoard().getId();
        commentService.delete(id);
        return "redirect:/board/detail/" + boardId;
    } /*
         댓글 삭제 후 상세 페이지로 돌아가기 위해
         댓글 ID가 아니라 댓글이 속한 게시글 ID로 리다이렉트되도록 수정했습니다.
         삭제 방식은 현재 GET으로 구현했지만,
         실무에서는 데이터 변경 요청이므로 POST 방식이 더 적절하다고 알고 있습니다.
      */
}


