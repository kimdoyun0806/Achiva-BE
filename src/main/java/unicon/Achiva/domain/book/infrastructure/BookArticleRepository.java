package unicon.Achiva.domain.book.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.book.entity.BookArticle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookArticleRepository extends JpaRepository<BookArticle, Long> {

    /**
     * 아티클 ID를 기반으로 해당 아티클이 속한 '모든' 책의 정보와 페이지 인덱스를 조회합니다.
     * 아티클은 여러 책에 속할 수 있으므로 List<BookArticle>로 반환합니다.
     * JOIN FETCH를 사용하여 N+1 문제 없이 연관된 Book 엔티티를 한 번의 쿼리로 함께 가져옵니다.
     *
     * @param articleId 조회할 아티클의 UUID
     * @return BookArticle 엔티티 리스트 (Book 정보 포함), 아티클이 책에 속하지 않으면 빈 리스트 반환
     */
    @Query("SELECT ba FROM BookArticle ba JOIN FETCH ba.book b WHERE ba.article.id = :articleId")
    Optional<List<BookArticle>> findBookInfosByArticleId(@Param("articleId") UUID articleId);

}