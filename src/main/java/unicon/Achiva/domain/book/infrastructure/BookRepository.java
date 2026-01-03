package unicon.Achiva.domain.book.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import unicon.Achiva.domain.book.entity.Book;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    // 특정 사용자가 만든 책 목록 조회
    Page<Book> findAllByMemberId(UUID memberId, Pageable pageable);

    // 소유자와 책 ID로 단건 조회
    Optional<Book> findByIdAndMemberId(UUID bookId, UUID memberId);

    // 소유자와 책 ID로 존재 여부 확인
    boolean existsByIdAndMemberId(UUID bookId, UUID memberId);
}
