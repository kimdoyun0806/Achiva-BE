package unicon.Achiva.domain.article.infrastructure;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.article.dto.SearchArticleCondition;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.category.Category;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {


    @Query("SELECT a.category, COUNT(a) FROM Article a WHERE a.member.id = :memberId GROUP BY a.category")
    List<Object[]> countArticlesByCategoryForMember(@Param("memberId") UUID memberId);

    /**
     * [수정됨] isBookTitle 필드 대신 LEFT JOIN을 사용하여 책의 대표(메인) 아티클을 피드에서 제외합니다.
     * Article(a)을 기준으로 Book(b) 테이블과 LEFT JOIN을 수행합니다.
     * 만약 아티클이 어떤 책의 대표 아티클(mainArticle)이라면 b.id가 존재할 것이고,
     * 그렇지 않다면 b.id는 NULL이 됩니다.
     * WHERE b.id IS NULL 조건을 통해 대표 아티클이 아닌 일반 아티클만 필터링합니다.
     */
    @EntityGraph(attributePaths = "member")
    @Query(value = """
            SELECT a
              FROM Article a
              LEFT JOIN Book b ON a.id = b.mainArticle.id
             WHERE (a.member.id IN :friendIds
                 OR a.member.id IN :cheererIds)
               AND b.id IS NULL
             ORDER BY
                 CASE WHEN a.member.id IN :friendIds THEN 0 ELSE 1 END,
                 a.createdAt DESC
            """,
            countQuery = """
                    SELECT COUNT(a)
                      FROM Article a
                      LEFT JOIN Book b ON a.id = b.mainArticle.id
                     WHERE (a.member.id IN :friendIds
                         OR a.member.id IN :cheererIds)
                       AND b.id IS NULL
                    """)
    Page<Article> findCombinedFeed(
            @Param("friendIds") Collection<UUID> friendIds,
            @Param("cheererIds") Collection<UUID> cheererIds,
            @ParameterObject Pageable pageable
    );

    @Modifying(flushAutomatically = true)
    @Query("""
              update Article a
                 set a.authorCategorySeq = a.authorCategorySeq - 1
               where a.member.id = :memberId
                 and a.category   = :category
                 and a.authorCategorySeq > :fromSeq
            """)
    int shiftLeft(@Param("memberId") UUID memberId,
                  @Param("category") Category category,
                  @Param("fromSeq") long fromSeq);


    Page<Article> searchByCondition(SearchArticleCondition condition, Pageable pageable);

    Page<Article> findAllByMemberId(UUID memberId, Pageable pageable);

    @EntityGraph(attributePaths = "member")
    Page<Article> findByCategoryIn(List<Category> categories, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.member.id = :memberId AND a.category = :category ORDER BY a.createdAt DESC")
    Page<Article> findByMemberIdWithCategory(UUID memberId, Category category, Pageable pageable);

    /**
     * 전체 게시글을 최신순으로 조회합니다.
     */
    @EntityGraph(attributePaths = "member")
    Page<Article> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * 응원 관계가 있는 사용자들이 작성한 게시글을 조회합니다.
     * @param memberId 현재 사용자 ID
     * @param pageable 페이징 정보 (기본 20개)
     * @return 응원 관계 사용자들의 게시글 페이지
     */
    @EntityGraph(attributePaths = "member")
    @Query(value = """
            SELECT DISTINCT a
              FROM Article a
              LEFT JOIN Book b ON a.id = b.mainArticle.id
              INNER JOIN Cheering c ON (c.sender.id = :memberId AND c.receiver.id = a.member.id AND c.isDeleted = false)
                                    OR (c.receiver.id = :memberId AND c.sender.id = a.member.id AND c.isDeleted = false)
             WHERE a.member.id <> :memberId
               AND a.isDeleted = false
               AND b.id IS NULL
             ORDER BY a.createdAt DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT a.id)
                      FROM Article a
                      LEFT JOIN Book b ON a.id = b.mainArticle.id
                      INNER JOIN Cheering c ON (c.sender.id = :memberId AND c.receiver.id = a.member.id AND c.isDeleted = false)
                                            OR (c.receiver.id = :memberId AND c.sender.id = a.member.id AND c.isDeleted = false)
                     WHERE a.member.id <> :memberId
                       AND a.isDeleted = false
                       AND b.id IS NULL
                    """)
    Page<Article> findByCheeringRelatedMembers(
            @Param("memberId") UUID memberId,
            Pageable pageable
    );

    /**
     * 특정 기간 동안 작성한 게시글의 총 글자 수를 조회합니다.
     * Question의 content 필드만 글자 수로 계산합니다.
     * @param memberId 사용자 ID
     * @param startDate 시작 일시 (null이면 제한 없음)
     * @param endDate 종료 일시 (null이면 제한 없음)
     * @return 총 글자 수
     */
    @Query("""
            SELECT COALESCE(SUM(LENGTH(q.content)), 0)
            FROM Article a
            JOIN a.questions q
            WHERE a.member.id = :memberId
            AND a.isDeleted = false
            AND (:startDate is null or a.createdAt >= :startDate)
            AND (:endDate is null or a.createdAt <= :endDate)
            """)
    long countTotalCharactersByDateRange(@Param("memberId") UUID memberId,
                                          @Param("startDate") java.time.LocalDateTime startDate,
                                          @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 카테고리별로 작성한 게시글의 글자 수를 조회합니다.
     * Question의 content 필드만 글자 수로 계산합니다.
     * @param memberId 사용자 ID
     * @param startDate 시작 일시 (null이면 제한 없음)
     * @param endDate 종료 일시 (null이면 제한 없음)
     * @return 카테고리별 글자 수 (Category, Long)
     */
    @Query("""
            SELECT a.category, COALESCE(SUM(LENGTH(q.content)), 0)
            FROM Article a
            JOIN a.questions q
            WHERE a.member.id = :memberId
            AND a.isDeleted = false
            AND (:startDate is null or a.createdAt >= :startDate)
            AND (:endDate is null or a.createdAt <= :endDate)
            GROUP BY a.category
            """)
    List<Object[]> countCharactersByCategoryAndDateRange(@Param("memberId") UUID memberId,
                                                          @Param("startDate") java.time.LocalDateTime startDate,
                                                          @Param("endDate") java.time.LocalDateTime endDate);
}