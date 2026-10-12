package com.example.consult.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "comment")
public class Comment { // 댓글 정보를 저장하는 JPA 엔티티
    // DB의 comment 테이블과 매핑

    // 댓글 고유 번호(PK), DB 에서 자동 증가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne // 여러 댓글을 한 명의 회원이 작성할수 있으므로 Member와 다대일(N:1) 관계 설정
    private Member member; // 댓글 작성자 (Member와 매핑) N:1 (다대일)

    // 어떤 게시글의 댓글인지 연결
    // 하나의 게시글에 여러 댓글이 작성될수 있으므로 Board와 다대일(N:1) 관계
    // comment 테이블의 boar_id 외래키(FK)를 사용하여 게시글과 연결
    // nullable = false 으로 설정해서 게시글 없이 댓글만 단독으로 존재할 수 없도록 함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board; // 댓글이 달린 게시글 (Board와 매핑)

    // 대댓글이 어떤 부모의 댓글에 속하는지 저장
    // 자기 자신(Comment)을 참조하는 다대일 관계
    // 일반 댓글은 parent가 null이고, 대댓글은 부모 Comment를 가짐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // 부모 댓글

    // 현재 댓글에 작성된 대댓글 목록
    // parent 필드를 기준으로 부모 댓글과 자식 댓글을 양방향으로 연결
    // 부모 댓글 삭제 시 연결된 대댓글도 함께 삭제
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private List<Comment> children = new ArrayList<>();  // 대댓글 목록

    private String content; // 댓글 내용

    private LocalDateTime createDate; // 댓글 작성일시

    private LocalDateTime updateDate; // 댓글 수정일시
}
