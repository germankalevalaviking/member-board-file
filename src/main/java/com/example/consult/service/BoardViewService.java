package com.example.consult.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardViewService {

    // 조회 기록을 저장할 쿠키의 이름을 "boardView"로 지정(상수)
    private static final String VIEW_COOKIE_NAME = "boardView";

    // 큐키 유지 시간(24시간 = 60초 × 60분 × 24시간)
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24; // 24시간

    // 게시글 조회수를 증가시키기 위한 서비스 객체
    private final BoardService boardService;

    /**
     * 게시글 첫 조회 시에만 조회수를 증가시키는 메인 메서드입니다.
     * @param boardId 게시글 ID
     * @param request 클라이언트의 요청 정보 객체 (쿠키를 읽어올 때 사용)
     * @param response 서버의 응답 정보 객체 (새로운 쿠키를 저장할 때 사용)
     */
    // 조회수 증가 여부를 판단하는 메인 메소드
    public void increaseViewCountIfFirstView(Long boardId,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        // 현재 요청(Request)에 boardView 쿠키가 있는지 조회
        Cookie viewCookie = findViewCookie(request);

        // 쿠키가 존재하지 않는다면(처음 방문)
        if (viewCookie == null) {
            // 조회수를 1 증가시키고 현재 게시글의 ID가 담긴 새로운 쿠키 생성해서 응답에 담아 보낸 후
            increaseViewAndCreateCookie(boardId, response);
            // 메소드 종료
            return;
        }
        // 쿠키는 있지만 현재 게시글을 아직 조회하지 않았다면
        // (해당 쿠키 안에 현재 보고 있는 게시글 ID가 포함되어 있지 않다면)
        if (!hasAlreadyViewed(viewCookie, boardId)) {
            // 조회수를 1 증가시키고 쿠키에 현재 게시글 ID를 추가하여 쿠키를 갱신
            increaseViewAndUpdateCookie(boardId, viewCookie, response);
        } /* 4. (암묵적 흐름) 기존 쿠키가 있고 해당 게시글 ID도 이미 포함되어 있다면 아무 동작도 하지 않음.
                (중복 조회 방지) */
    }

    // 클라이언트의 요청에서 "boardView"라는 이름을 가진 쿠키를 찾아 반환하는 메서드
    private Cookie findViewCookie(HttpServletRequest request) {
        // 클라이언트의 요청에 담긴 모든 쿠키 배열을 가져옴.(브라우저에서 전달된 모든 쿠키 가져오기)
        Cookie[] cookies = request.getCookies();

        // 가져온 쿠키 배열이 비어있다면(쿠키가 하나도 없다면)
        if (cookies == null) {
            //  null을 반환
            return null;
        }
        // 쿠키 배열을 하나씩 순회하며 꺼냄(모든 쿠키를 하나씩 확인)
        for (Cookie cookie : cookies) {
            // 쿠키의 이름이 우리가 지정한 "boardView"와 일치하는지 확인(boardView 쿠키라면)
            if (VIEW_COOKIE_NAME.equals(cookie.getName())) {
                return cookie; // 일치하는 쿠키를 찾으면 즉시 해당 쿠키 객체를 반환
            }
        }
        // 순회를 마쳤는데도 "boardView" 쿠키가 없다면 null을 반환
        return null;
    }

    // 현재 조회하려는 게시글이 이미 쿠키에 등록되어(이미 본 글인지) 있는지 확인하는 메서드
    private boolean hasAlreadyViewed(Cookie cookie, Long boardId) {
        // 쿠키의 문자열 값에 "[게시글ID]" 형태의 문자열이 포함되어 있는지 여부를 true/false로 반환
        // 예: 쿠키 값이 "[1]_[5]" 일 때, boardId가 1이면 true, 3이면 false를 반환
        return cookie.getValue().contains("[" + boardId + "]");
    }

    // 쿠키가 전혀 없을 때, 조회수를 올리고 쿠키를 새로 생성하는 메서드
    private void increaseViewAndCreateCookie(Long boardId,
                                             HttpServletResponse response) {
        // DB의 해당 게시글 조회수를 1 증가
        boardService.increaseView(boardId);

        // 이름은 "boardView", 값은 "[게시글ID]" 형태로 새로운 쿠키 객체를 생성. (예: "[1]")
        Cookie cookie = new Cookie(VIEW_COOKIE_NAME, "[" + boardId + "]");
        cookie.setPath("/"); // 이 쿠키가 웹사이트 전체 경로("/")에서 유효하도록 설정
        cookie.setMaxAge(COOKIE_MAX_AGE); // 쿠키의 만료 시간을 24시간으로 설정

        // 생성된 쿠키를 클라이언트의 브라우저에 저장하도록 응답(response)에 추가
        response.addCookie(cookie);
    }

    // 쿠키는 이미 존재하지만 새로운 게시글을 조회했을 때, 조회수를 올리고 기존 쿠키 값을 갱신하는 메서드
    private void increaseViewAndUpdateCookie(Long boardId,
                                             Cookie cookie,
                                             HttpServletResponse response) {
        // DB의 해당 게시글 조회수를 1 증가
        boardService.increaseView(boardId);

        // 기존 쿠키의 문자열 값 뒤에 구분자(_)와 함께 새로운 게시글 ID를 붙여서 값을 변경.
        // 예: 기존 값이 "[1]"이었다면 "[1]_[3]"으로 업데이트
        cookie.setValue(cookie.getValue() + "_[" + boardId + "]");
        cookie.setPath("/"); // 쿠키의 유효 경로를 재설정
        cookie.setMaxAge(COOKIE_MAX_AGE); // 쿠키의 만료 시간을 다시 24시간으로 연장(갱신).

        // 변경된 쿠키를 클라이언트 브라우저에 덮어쓰기 위해 응답(response)에 추가
        response.addCookie(cookie);
    }
}
