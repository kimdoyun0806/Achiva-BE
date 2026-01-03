package unicon.Achiva.domain.book;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.article.ArticleErrorCode;
import unicon.Achiva.domain.article.ArticleService;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.article.infrastructure.ArticleRepository;
import unicon.Achiva.domain.book.dto.BookRequest;
import unicon.Achiva.domain.book.dto.BookResponse;
import unicon.Achiva.domain.book.entity.Book;
import unicon.Achiva.domain.book.entity.BookArticle;
import unicon.Achiva.domain.book.infrastructure.BookRepository;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.global.response.GeneralException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    private final ArticleService articleService;

    @Transactional(readOnly = true)
    protected Member getCurrentMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 책 생성
     */
    @Transactional
    public BookResponse createBook(BookRequest request, UUID memberId) {

        Member member = getCurrentMember(memberId);

        Article mainArticle = articleService.createArticleEntity(request.getMain(), memberId);

        Book book = Book.builder()
                .member(member)
//                .title(request.getTitle())
//                .description(request.getDescription())
                .build();

        if (request.getArticleIds() != null && !request.getArticleIds().isEmpty()) {
            List<Article> articles = articleRepository.findAllById(request.getArticleIds());
            int idx = 0;
            for (Article article : articles) {
                book.addArticle(article, idx++);
            }
        }

        Book savedBook = bookRepository.save(book);
        savedBook.setMainArticle(mainArticle);

        return BookResponse.fromEntity(savedBook);
    }

    /**
     * 책 단건 조회
     */
    @Transactional(readOnly = true)
    public BookResponse getBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new GeneralException(BookErrorCode.BOOK_NOT_FOUND));
        return BookResponse.fromEntity(book);
    }

    /**
     * 모든 책 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(BookResponse::fromEntity);
    }

    /**
     * 책 수정
     */
    @Transactional
    public BookResponse updateBook(UUID bookId, BookRequest request, UUID memberId) {

        Book book = bookRepository.findByIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new GeneralException(BookErrorCode.UNAUTHORIZED_BOOK_ACCESS));

//        book.update(request.getTitle(), request.getDescription());

        List<UUID> newArticleIds = request.getArticleIds();
        if (newArticleIds != null && !newArticleIds.isEmpty()) {

            // [Step A] 기존 관계 싹 끊기 (OrphanRemoval로 인해 DB Delete 예정)
            book.getBookArticles().clear();

            // [Step B] DB에 DELETE 쿼리 즉시 전송 (인덱스 충돌 방지 & 빈 공간 확보)
            bookRepository.flush();

            // [Step C] 새 아티클 목록 조회
            List<Article> articles = articleRepository.findAllById(newArticleIds);

            if (articles.size() != new HashSet<>(newArticleIds).size()) {
                throw new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND);
            }

            Map<UUID, Article> articleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, Function.identity()));

            for (UUID articleId : newArticleIds) {
                book.addArticle(articleMap.get(articleId));
            }
        }

        return BookResponse.fromEntity(book);
    }

    /**
     * 책 삭제
     */
    @Transactional
    public void deleteBook(UUID bookId, UUID memberId) {
        Book book = bookRepository.findByIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new GeneralException(BookErrorCode.UNAUTHORIZED_BOOK_ACCESS));
        bookRepository.delete(book);
    }

    /**
     * 페이지(Article) 추가
     */
    @Transactional
    public BookResponse addArticleToBook(UUID bookId, UUID articleId, UUID memberId) {
        Book book = bookRepository.findByIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new GeneralException(BookErrorCode.UNAUTHORIZED_BOOK_ACCESS));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        int index = book.getBookArticles().size();
        book.addArticle(article, index);
        return BookResponse.fromEntity(book);
    }

    /**
     * 페이지(Article) 제거
     */
    @Transactional
    public BookResponse removeArticleFromBook(UUID bookId, UUID articleId, UUID memberId) {
        Book book = bookRepository.findByIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new GeneralException(BookErrorCode.UNAUTHORIZED_BOOK_ACCESS));

        BookArticle targetBookArticle = book.getBookArticles().stream()
                .filter(ba -> ba.getArticle().getId().equals(articleId))
                .findFirst()
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        book.getBookArticles().remove(targetBookArticle);

        // [핵심] 3. 강제로 DB에 반영하여 DELETE 쿼리를 먼저 날림!
        // 이렇게 하면 DB에서 해당 인덱스 자리가 비게 됨.
        bookRepository.flush();

        // 4. 이제 빈 자리가 생겼으니 안전하게 재정렬 (UPDATE)
        book.reorderIndices();

        // 트랜잭션이 끝나면서 재정렬된 UPDATE 쿼리가 나감
        return BookResponse.fromEntity(book);
    }

    /**
     * 특정 사용자가 만든 책 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksByMember(UUID memberId, Pageable pageable) {
        return bookRepository.findAllByMemberId(memberId, pageable)
                .map(BookResponse::fromEntity);
    }
}
