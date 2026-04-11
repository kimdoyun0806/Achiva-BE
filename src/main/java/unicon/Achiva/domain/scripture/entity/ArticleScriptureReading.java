package unicon.Achiva.domain.scripture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.article.entity.Article;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "article_scripture_reading")
public class ArticleScriptureReading {

    @Id
    @Column(name = "article_id", columnDefinition = "BINARY(16)")
    private UUID articleId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Article article;

    @Column(name = "scripture_id", nullable = false, length = 50)
    private String scriptureId;

    @Column(name = "start_chapter", nullable = false)
    private Integer startChapter;

    @Column(name = "end_chapter", nullable = false)
    private Integer endChapter;

    @Column(name = "completed_chapters", nullable = false)
    private Integer completedChapters;

    @Column(name = "read_at", nullable = false)
    private LocalDate readAt;

    public static ArticleScriptureReading create(
            Article article,
            String scriptureId,
            Integer startChapter,
            Integer endChapter,
            Integer completedChapters,
            LocalDate readAt
    ) {
        return ArticleScriptureReading.builder()
                .article(article)
                .articleId(article.getId())
                .scriptureId(scriptureId)
                .startChapter(startChapter)
                .endChapter(endChapter)
                .completedChapters(completedChapters)
                .readAt(readAt)
                .build();
    }
}
