package com.example.consult.service;

import com.example.consult.entity.Member;
import com.example.consult.dto.BoardDto;
import com.example.consult.entity.Board;
import com.example.consult.repository.BoardRepository;
import com.example.consult.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.consult.entity.Comment_.board;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    // 실제 로컬 디스크(또는 S3 등)의 물리 파일 삭제 작업과 DB 상태 동기화를 일관성 있게 처리

    @Value("${file.dir}") // application.properties 등에 설정한 파일 저장 기본 경로
    private String fileDir;

    private final BoardRepository boardRepository;

    private final MemberRepository memberRepository;

    // 조회수 증가를 위한 트랜잭션 메서드
    @Transactional
    public void increaseView(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 게시글을 찾을수 없습니다. id=" + id));
        board.increaseViewCount();
    }

    @Transactional
    public Long saveBoard(BoardDto boardDto, String loginEmail) throws IOException {
        // 작성한 회원 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원정보를 찾을 수 없습니다."));

        // 파일 처리 로직 (파일이 있으면 이름 생성 및 디스크 저장)
        MultipartFile file = boardDto.getFile();
        String originalFileName = null; // 아직 원본 파일이 없는 상태
        String storedFileName = null; //  저장된 파일이 없는 상태

        if (file != null && !file.isEmpty()) { // 파일 유무 검사
            originalFileName = file.getOriginalFilename(); //파일의 원본파일을 가져옴
            String uuid = UUID.randomUUID().toString(); // 중복 방지를 위한 36자리 고유 UUID 생성
            storedFileName = uuid + "_" + originalFileName; // 서버에 저장될 파일 이름 설정

            // 파일 객체에 파일 경로와 저장된 파일이름을 가져와서 saveFile 변수에 저장
            File saveFile = new File(fileDir, storedFileName);
            if (!saveFile.getParentFile().exists()) { // 부보 디엑터리(폴더)이 없다면
                saveFile.getParentFile().mkdir(); // mkdirs()로 상위 디렉터리까지 안전 생성
            }
            // file.transferTo()로 실제 서버 폴더에 저장.
            file.transferTo(saveFile);
        }
            // Board 엔티티 객체에 필드를 세팅해서 저장
            Board board = new Board(boardDto.getTitle(),
                    boardDto.getContent(),
                    member, originalFileName, storedFileName);
            boardRepository.save(board);
            return board.getId();
    }

    @Transactional
    public void update(Long id, BoardDto boardDto) throws IOException{
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + id));

        // 1. 기본 제목, 내용 수정 (Setter 대신 도메인 비즈니스 메서드 호출)
        board.updateContent(boardDto.getTitle(), boardDto.getContent());

        // 2. 수정 폼에서 새로 첨부한 파일이 있는지 확인
        MultipartFile file = boardDto.getFile(); // 파일 업로드 처리 시작
        if(file != null && !file.isEmpty()) {

//            // (선택 사항) 기존에 저장되어 있던 실제 파일 삭제 처리
//            if(board.getStoredFileName() != null) {
//                File deleteFile = new File(fileDir, board.getStoredFileName());
//                if(!deleteFile.exists()) {
//                    deleteFile.delete();
//                }
//            }

            // 3. 새 파일 저장 (saveBoard 할 때와 동일한 로직)
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String storedFileName = uuid + "_" + originalFilename;

            // 실제 파일을 저장할 위치 생성
            File saveFile = new File(fileDir, storedFileName);
            // saveFile이 위치할 상위 디렉터리(폴더)가 실제로 존재하는지 확인하고, 만약 존재하지 않는다면 (!exists)
            if(!saveFile.getParentFile().exists()) {
                // 해당 경로에 필요한 모든 상위 디렉터리를 한 번에 생성함
                saveFile.getParentFile().mkdir();
            }
            // file.transferTo()로 실제 서버 폴더에 저장.
            file.transferTo(saveFile);

            // 4. Entity의 파일 정보 업데이트 (Setter 대신 전용 메서드 호출)
            board.updateFile(originalFilename, storedFileName);
        }
    }

    @Transactional
    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + id));


        if(board.getStoredFileName() != null) {  // 1. 서버 폴더에 저장된 파일이 있는지 확인
            File file = new File(fileDir, board.getStoredFileName());
            if(file.exists()) {  // 2. 실제 파일이 존재하면 삭제
                file.delete();
            }
        }
        // DB 데이터 삭제 (연관된 Comment는 CascadeType.REMOVE로 자동 삭제)
        boardRepository.delete(board);
    }

    // DB에 데이터가 수만 건 쌓였을 때 모든 데이터를 한 번에 메모리로 끌어올려 OOM(Out Of Memory)을 유발할 수 있음.
    /*  실제 화면 목록 조회가 getList(int page, Specification<Board> spec)를 통해 페이징으로 이루어진다면,
        전체 조회가 필요한 특수 목적이 아닌 이상 list()는 사용을 지양하거나 주석 처리하는 편이 안전 */
//    public List<Board> list() { // 리스트
//        return boardRepository.findAll();
//    }

    public Board detail(Long id) { // 상세 보기
        return boardRepository.findById(id).
                orElseThrow(() ->
                        new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + id));
    }

    public List<Board> list() {
        return boardRepository.findAll();
    }

    // 페이징 처리된 게시글 조회
    public Page<Board> getList(int page, Specification<Board> spec) {
        // PageRequest.of(페이지 번호, 한 페이지에 보여줄 개수, 정렬 기준)
        PageRequest pageable =
                // -> page 번째 페이지, 게시글 10개씩, 작성일 기준 내림차순
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "regTime"));
        // 조건(spec) + 페이징(pageable)을 적용해서 게시글 목록 반환
        return boardRepository.findAll(spec, pageable);
    }

    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        // 또는 .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
    }

}
