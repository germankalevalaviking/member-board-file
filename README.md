# 📌 Spring Boot & JPA 기반 커뮤니티 게시판 프로젝트 (member-Board-file)

> Spring Boot, Spring Data JPA, Thymeleaf를 활용하여 개발한 계층형 댓글 지원 커뮤니티 게시판 웹 애플리케이션입니다.  
> 객체 지향적 도메인 설계와 실무형 예외 처리, 데이터 무결성을 고려한 CRUD 및 파일 업로드/다운로드 시스템을 구현했습니다.

---

## 🛠 1. 개발 환경 및 기술 스택

### Backend
- Language: Java 17
- Framework: Spring Boot 3.3.5
- ORM / Persistence Spring Data JPA, Hibernate, QueryDSL
- Security: Spring Security
- Template Engine: Thymeleaf

### Database & Tools
- **Database**: MySQL 8.0
- **Connection Pool**: HikariCP, P6Spy (SQL 쿼리 로깅)
- **Build Tool**: Gradle
- **VCS**: Git, GitHub

---

## ✨ 2. 핵심 기능 요약

 1) 회원 관리 (Member)
- Spring Security 기반의 회원가입 및 로그인/로그아웃 처리
- 권한 검증 및 세션/인증 정보를 통한 작성자 식별

 2) 게시글 관리 (Board)
- CRUD: 게시글 작성, 상세 조회, 수정, 삭제
- 페이징 & 검색: Spring Data JPA Pageable을 활용한 페이징 처리 및 검색 기능
- 첨부파일: 파일 업로드 및 다운로드 기능 (서버 파일명 UUID 암호화/저장 분리)
- 조회수 처리: 게시글 진입 시 조회수 자동 증가 로직 반영

 3) 계층형 댓글 및 대댓글 시스템 (Comment)
- **자기참조(Self-Referencing) 모델링**: 단일 엔티티 구조에서 부모 댓글(`parent`)과 자식 대댓글 리스트(`children`) 매핑
- **원댓글 필터링**: `parent IS NULL` 조건 조회를 통한 원댓글 단위 리스트 바인딩
- **지연 로딩 순회**: Thymeleaf 뷰 템플릿에서 `comment.children` 탐색을 통한 대댓글 들여쓰기 출력
- **고아 객체 및 종속 삭제**: 부모 댓글 삭제 시 종속된 대댓글 일괄 정리 (`CascadeType.REMOVE`)

---

## 🏗 3. 도메인 구조 및 연관관계 설계

```text
[ Member ] 1 ─── N [ Board ]
   │                    │
   │ 1                  │ 1
   │                    │
   └─── N [ Comment ] N ┘
               │ 1 (parent)
               │
               └── N (children) [ Comment ] (자기참조)
