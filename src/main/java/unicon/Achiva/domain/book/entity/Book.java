package unicon.Achiva.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.global.common.UuidBaseEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends UuidBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member; // ✅ Book 생성자 (소유자)

//    private String title; // 책 제목
//    private String description; // 설명

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "main_article_id")
    private Article mainArticle; // 첫 페이지 역할의 메인 아티클

    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageIndex ASC")
    private List<BookArticle> bookArticles = new ArrayList<>();

    /**
     * 새 페이지 추가
     */
    public void addArticle(Article article, int index) {
        // 중복 체크
        boolean alreadyExists = bookArticles.stream()
                .anyMatch(ba -> ba.getArticle().equals(article));

        if (!alreadyExists) {
            BookArticle bookArticle = BookArticle.builder()
                    .book(this)
                    .article(article)
                    .pageIndex(index)
                    .build();

            this.bookArticles.add(bookArticle);
            article.getBookArticles().add(bookArticle);
        }
    }

    /**
     * 페이지 순서 변경
     */
    public void updateArticleIndex(Article article, int newIndex) {
        bookArticles.stream()
                .filter(ba -> ba.getArticle().equals(article))
                .findFirst()
                .ifPresent(ba -> ba.updatePageIndex(newIndex));
    }

    /**
     * 제목/설명 변경
     */
//    public void update(String newTitle, String newDesc) {
//        this.title = newTitle;
//        this.description = newDesc;
//    }

    public void addArticle(Article article) {
        BookArticle bookArticle = BookArticle.builder()
                .book(this)
                .article(article)
                .pageIndex(this.bookArticles.size()) // 현재 리스트 크기가 곧 다음 인덱스
                .build();

        this.bookArticles.add(bookArticle);
    }

    /**
     * 내부 메서드: 인덱스 0부터 순서대로 재할당
     */
    public void reorderIndices() {
        // 혹시 모를 순서 꼬임 방지를 위해 현재 pageIndex 기준으로 정렬
        this.bookArticles.sort(Comparator.comparingInt(BookArticle::getPageIndex));

        // 0부터 차례대로 인덱스 업데이트
        for (int i = 0; i < this.bookArticles.size(); i++) {
            this.bookArticles.get(i).updatePageIndex(i);
        }
    }
}