package com.example.consult.service;

import com.example.consult.entity.Board;
import com.example.consult.entity.Comment;
import com.example.consult.entity.Member;
import com.example.consult.repository.BoardRepository;
import com.example.consult.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // 일반 댓글은 createComment()
    // 대댓글은 createReply

    // 원 댓글 생성(작성)
    // parent 설정이 없으므로 parent 필드는 자동으로 null 이 됨
    public Comment createComment(Board board, Member member, String content) {
        Comment comment = new Comment(); // 새로운 댓글 객체 생성
        comment.setBoard(board); // 어떤 게시글에 달린 댓글인지 연결.(연관관계 매핑)
        comment.setMember(member); // // 댓글 작성자 연결.(댓글 작성자 연관관계 매핑)
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());

        // 완성된 댓글을 db에 저장
        comment = this.commentRepository.save(comment);
        return comment;
    }

    // 대댓글(생성) 작성
    // 전달 받은 parentId를 기반으로 원댓글을 찾아 부모 자식 연관관계(parent)를 매핑
    public void createReply(Board board, Member member, Integer parentId, String content) {
        // 전달받은 parentId로 DB에서 부모 댓글 엔티티를 조회.
        Comment parentComment = commentRepository.findById(parentId)
                // 조회 결과가 null이면(없으면) 예외를 발생시켜 이후 로직을 중단합니다.
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

        // 생성된 대댓글 객체에 방금 조회한 부모 댓글(parentComment) 객체를 연결(매핑)
        Comment reply = new Comment();
        reply.setBoard(board);
        reply.setMember(member);

        // 부모 댓글 객체를 세팅하여 대댓글로 식별 (parent != null)
        reply.setParent(parentComment);
        reply.setContent(content);
        reply.setCreateDate(LocalDateTime.now());

        commentRepository.save(reply); // 대댓글(reply) 자체를 데이터베이스의 comment 테이블에 새로운 행(Row)으로 저장
    }

    public List<Comment> getComments(Board board) {
        return commentRepository.findByBoardAndParentIsNullOrderByCreateDateAsc(board);
    }

    // 댓글 (목록) 조회
    public List<Comment> getListByBoard(Board board) {
        return commentRepository.findByBoardAndParentIsNullOrderByCreateDateAsc(board);
    }

    // 댓글 수정
    public Comment updateComment(Integer id, String content) {
        // 댓글 id로 댓글을 조회
        Comment comment = commentRepository.findById(id)
                // 예외 처리
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
        comment.setContent(content); // 댓글 내용 설정
        comment.setUpdateDate(LocalDateTime.now()); // 수정 일시 설정
        return commentRepository.save(comment); // 수정(설정)한 결과를 리턴
    }

    public void delete(Integer id) {
        commentRepository.deleteById(id);
    }
}
