package com.example.consult.repository;

import com.example.consult.entity.Board;
import com.example.consult.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


 // Comment → 관리할 Entity
 // Integer → Comment의 PK(id) 타입
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // 특정 게시글에 작성된 일반 댓글(parent가 없는 댓글)만 조회.
    // 댓글 작성시간(createDate)을 기준으로 오래된 순(ASC)으로 정렬.
    List<Comment> findByBoardAndParentIsNullOrderByCreateDateAsc(Board board);
}
