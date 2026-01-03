package unicon.Achiva.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.book.entity.Book;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private UUID id;
    private String title;
    private String description;
    private ArticleResponse mainArticle;
    private List<ArticleResponse> articles;

    public static BookResponse fromEntity(Book book) {
        return BookResponse.builder()
                .id(book.getId())
//                .title(book.getTitle())
//                .description(book.getDescription())
                .mainArticle(book.getMainArticle() != null
                        ? ArticleResponse.fromEntity(book.getMainArticle())
                        : null)
                .articles(book.getBookArticles().stream()
                        .map(bookArticle -> ArticleResponse.fromEntity(bookArticle.getArticle()))
                        .collect(Collectors.toList()))
                .build();
    }
}