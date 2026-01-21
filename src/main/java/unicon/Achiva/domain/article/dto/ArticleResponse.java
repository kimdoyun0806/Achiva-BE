package unicon.Achiva.domain.article.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.category.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
public class ArticleResponse {
    private UUID id;
    private List<String> photoUrls;
    private String title;
    private Category category;
    private List<ArticleRequest.QuestionDTO> question;
    private UUID memberId;
    private String memberNickName;
    private String memberProfileUrl;
    private String backgroundColor;
    private Long authorCategorySeq;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
//    private boolean isBookTitle;

    protected static <B extends ArticleResponseBuilder<?, ?>> B initBuilder(B builder, Article article) {
        return (B) builder
                .id(article.getId())
                .photoUrls(article.getPhotoUrls())
                .title(article.getTitle())
                .category(article.getCategory())
                .question(
                        article.getQuestions()
                                .stream()
                                .map(ArticleRequest.QuestionDTO::fromEntity)
                                .toList()
                )
                .memberId(article.getMember().getId())
                .memberNickName(article.getMember().getNickName())
                .memberProfileUrl(article.getMember().getProfileImageUrl())
                .authorCategorySeq(article.getAuthorCategorySeq())
                .backgroundColor(article.getBackgroundColor())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt());
    }

    public static ArticleResponse fromEntity(Article article) {
        // 내부적으로 initBuilder를 쓰고 바로 build()
        return initBuilder(ArticleResponse.builder(), article).build();
    }
}
