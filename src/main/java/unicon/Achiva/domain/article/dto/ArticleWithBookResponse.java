package unicon.Achiva.domain.article.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.book.entity.BookArticle;

import java.util.List;

@Getter
@SuperBuilder
public class ArticleWithBookResponse extends ArticleResponse {
    private List<BookArticleInfoResponse> bookArticle;

    public static ArticleWithBookResponse fromEntity(Article article, List<BookArticle> bookArticle) {
        return initBuilder(ArticleWithBookResponse.builder(), article)
                .bookArticle(
                        bookArticle.stream()
                                .map(BookArticleInfoResponse::from)
                                .toList()
                )
                .build();
    }
}
