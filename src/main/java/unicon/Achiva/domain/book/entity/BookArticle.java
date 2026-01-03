package unicon.Achiva.domain.book.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.article.entity.Article;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "book_article",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_book_page_index",
                        columnNames = {"book_id", "page_index"}
                )
        }
)
public class BookArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Article article;

    @Column(name = "page_index", nullable = false)
    private Integer pageIndex;

    // 연관관계 편의 메서드
    public void updateBook(Book book) {
        this.book = book;
    }

    public void updateArticle(Article article) {
        this.article = article;
    }

    public void updatePageIndex(Integer index) {
        this.pageIndex = index;
    }
}