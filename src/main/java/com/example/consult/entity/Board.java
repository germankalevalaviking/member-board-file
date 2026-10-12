package com.example.consult.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
/* @Setter 남용 지양 (엔티티 캡슐화 및 불변성 유지)
   클래스 레벨의 @Setter는 외부에서 엔티티의 상태를 무분별하게 변경할 수 있어
    어디서 데이터가 오염되었는지 추적하기 어렵게 만듬 */
//@Setter
// 무분별한 기본 생성자 호출 방지
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "board")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 PK, DB의 AUTO_INCREMENT 방식으로 값 생성

    @Column(nullable = false, length = 50)
    private String title; // 제목

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 게시글 작성자(Member) 다대일 연관관계 매핑

    // 긴 본문 글이 잘리지 않도록 설정
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    private LocalDateTime regTime; // 작성 시간

    // 조회수 필드 (기본값 0으로 설정)
    private int viewCount = 0;

    // == 비즈니스 로직 메서드 == //
    // 조회수 증가 비즈니스 메소드
    public void increaseViewCount() {
        this.viewCount += 1;
    }

    // cascade = CascadeType.REMOVE :부모 삭제 시 자식도 삭제
    // orphanRemoval = true : 컬렉션에서 빠진 고아 엔티티 제거
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    // 파일 첨부 필드 (파일의 원본 이름과 서버 저장용 이름을 보관하는 필드를 추가)
    private String originalFileName; // 사용자가 업로드한 실제 파일명 (예: resume.pdf)
    private String storedFileName; // 서버 내부 저장용 파일명 (예: uuid_resume.pdf)

    // 게시글 생성 시 필요한 값을 초기화하는 생성자
    public Board(String title, String content, Member member,
                 String originalFileName, String storedFileName) {
        // 필드 초기화
        this.title = title;
        this.content = content;
        this.member = member;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.viewCount = 0;
        this.regTime = LocalDateTime.now();
    }

    // == 비즈니스 로직 메서드 == //
    public void update(String title, String content,
                       String originalFileName, String storedFileName) {
        // 게시글 제목과 내용을 전달받은 값으로 변경
        this.title = title;
        this.content = content;

        // 저장된 파일이름이 null이 아닌 경우
        // 새 첨부파일이 존재하는 경우에만 파일 정보 변경
        if(storedFileName != null) {
            // 해당 원본 파일과 저장된 파일을 변경
            this.originalFileName = originalFileName;
            this.storedFileName = storedFileName;
        }
    }

    // == 연관관계 편의 메서드 == //
    public void addComment(Comment comment) {
        // 해당(클래스의) 댓글목록에 comment 객체를 추가
        this.commentList.add(comment);
        comment.setBoard(this); // Comment 쪽에 board 지정
    }

    // == 비즈니스 수정 메소드 == //
    // 엔티티 내부에서는 제목/본문 수정 로직과 파일 변경/삭제 로직의 역할을 명확히 나눔.(메서드 분리)

    // 기본 텍스트 정보(제목, 본문) 수정
    public void updateContent(String title, String content) {
        this.title = title; // 해당 필드를 파라미터로 받은 title로 초기화(변경)
        this.content = content; // 해당 필드를 파라미터로 받은 content로 초기화(변경)
    } // 텍스트 데이터(제목, 본문) 수정에만 집중하며, 파일 관련 상태에는 일절 관여하지 않음.

    // 새 파일로 교체할 때 호출
    public void updateFile(String originalFileName, String storedFileName) {
        // 해당 필드를 파라미터로 받은 originalFileName으로 초기화
        this.originalFileName = originalFileName;
        // 해당 필드를 파라미터로 받은 storedFileName으로 초기화
        this.storedFileName = storedFileName;
    } // 기존 파일이 있든 없든 새로운 첨부파일 정보로 교체하는 역할만 수행

    // 기존 파일을 완전히 제거할 때 호출
    public void removeFile() {
        this.originalFileName = null; // 필드를 null로 설정
        this.storedFileName = null; // 필드를 null로 초기화
    } // 파일 정보를 null로 비워 명시적으로 파일 삭제 상태를 만듭니다.적으로 파일 삭제 상태를 만듭니다.
}
