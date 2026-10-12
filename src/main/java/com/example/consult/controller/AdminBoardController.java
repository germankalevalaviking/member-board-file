package com.example.consult.controller;

import com.example.consult.entity.Board;
import com.example.consult.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/consult")
public class AdminBoardController {

    private final BoardService boardService;

    @GetMapping("/list")
    public String list(Model model) {
        List<Board> boardList = boardService.list();
        model.addAttribute("boardList", boardList);
        return "admin/board/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Board board = boardService.detail(id);
        model.addAttribute("board", board);
        return "admin/board/detail";
    }
}
