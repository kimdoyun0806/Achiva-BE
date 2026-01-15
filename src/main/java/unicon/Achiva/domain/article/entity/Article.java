package unicon.Achiva.domain.article.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.article.dto.ArticleRequest;
import unicon.Achiva.domain.book.entity.BookArticle;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.cheering.entity.Cheering;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.global.common.UuidBaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "article")
public class Article extends UuidBaseEntity {

    private String photoUrl;

    private String title;

//    private Boolean isBookTitle;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Category category;

    private String backgroundColor;

    @Column(name = "author_category_seq", nullable = false)
    private Long authorCategorySeq;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "article", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Cheering> cheerings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookArticle> bookArticles = new ArrayList<>();

    // ---- 로직 ----
    public void update(ArticleRequest request) {
        this.photoUrl = request.photoUrl();
        this.title = request.title();
        this.backgroundColor = request.backgroundColor();

        this.questions.clear();
        for (ArticleRequest.QuestionDTO questionDTO : request.question()) {
            Question question = ArticleRequest.QuestionDTO.toEntity(questionDTO);
            question.setArticle(this);
            this.questions.add(question);
        }
    }

    public void changeCategoryAndSeq(Category newCategory, long newSeq) {
        this.category = newCategory;
        this.authorCategorySeq = newSeq;
    }
}