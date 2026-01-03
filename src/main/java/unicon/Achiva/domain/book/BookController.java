package unicon.Achiva.domain.book;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.book.dto.BookRequest;
import unicon.Achiva.domain.book.dto.BookResponse;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Tag(name = "Book API", description = "Book 및 페이지(Article) 관리 API")
public class BookController {

    private final BookService bookService;
    private final AuthService authService;

    /**
     * ✅ 책 생성
     */
    @Operation(summary = "책 생성", description = "로그인한 사용자가 새로운 책(Book)을 생성합니다.")
    @PostMapping
    public BookResponse createBook(@RequestBody BookRequest request) {
        UUID memberId = authService.getMemberIdFromToken();
        return bookService.createBook(request, memberId);
    }

    /**
     * ✅ 내 책 목록 조회 (페이징)
     */
    @Operation(summary = "내 책 목록 조회", description = "로그인한 사용자가 생성한 책 목록을 페이징하여 조회합니다.")
    @GetMapping("/my")
    public Page<BookResponse> getMyBooks(@ParameterObject Pageable pageable) {
        UUID memberId = authService.getMemberIdFromToken();
        return bookService.getBooksByMember(memberId, pageable);
    }

    /**
     * ✅ 단일 책 상세 조회
     */
    @Operation(summary = "책 상세 조회", description = "책 ID를 기반으로 단일 Book 정보를 조회합니다.")
    @GetMapping("/{bookId}")
    public BookResponse getBook(
            @Parameter(description = "조회할 Book의 ID") @PathVariable UUID bookId) {
        return bookService.getBook(bookId);
    }

    /**
     * ✅ 모든 책 조회 (공개 피드 or 관리자용)
     */
    @Operation(summary = "전체 책 조회", description = "모든 Book을 페이징 형태로 조회합니다. (관리자 또는 공개용)")
    @GetMapping
    public Page<BookResponse> getAllBooks(@ParameterObject Pageable pageable) {
        return bookService.getAllBooks(pageable);
    }

    /**
     * ✅ 책 수정
     */
    @Operation(summary = "책 수정", description = "로그인한 사용자가 본인의 책 정보를 수정합니다.")
    @PutMapping("/{bookId}")
    public BookResponse updateBook(
            @Parameter(description = "수정할 Book의 ID") @PathVariable UUID bookId,
            @RequestBody BookRequest request) {
        UUID memberId = authService.getMemberIdFromToken();
        return bookService.updateBook(bookId, request, memberId);
    }

    /**
     * ✅ 책 삭제
     */
    @Operation(summary = "책 삭제", description = "로그인한 사용자가 본인의 책을 삭제합니다.")
    @DeleteMapping("/{bookId}")
    public void deleteBook(
            @Parameter(description = "삭제할 Book의 ID") @PathVariable UUID bookId) {
        UUID memberId = authService.getMemberIdFromToken();
        bookService.deleteBook(bookId, memberId);
    }

    /**
     * ✅ 책에 Article 추가
     */
    @Operation(summary = "책에 Article 추가", description = "특정 Book에 Article을 추가합니다.")
    @PostMapping("/{bookId}/articles/{articleId}")
    public BookResponse addArticle(
            @Parameter(description = "대상 Book의 ID") @PathVariable UUID bookId,
            @Parameter(description = "추가할 Article의 ID") @PathVariable UUID articleId) {
        UUID memberId = authService.getMemberIdFromToken();
        return bookService.addArticleToBook(bookId, articleId, memberId);
    }

    /**
     * ✅ 책에서 Article 제거
     */
    @Operation(summary = "책에서 Article 제거", description = "특정 Book에서 지정된 Article을 제거합니다.")
    @DeleteMapping("/{bookId}/articles/{articleId}")
    public BookResponse removeArticle(
            @Parameter(description = "대상 Book의 ID") @PathVariable UUID bookId,
            @Parameter(description = "제거할 Article의 ID") @PathVariable UUID articleId) {
        UUID memberId = authService.getMemberIdFromToken();
        return bookService.removeArticleFromBook(bookId, articleId, memberId);
    }
}
