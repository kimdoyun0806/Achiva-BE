package unicon.Achiva.domain.article.dto;

import unicon.Achiva.domain.book.entity.BookArticle;

import java.util.UUID;

public record BookArticleInfoResponse(
        UUID bookId,
        String bookTitle
//        int pageIndex
) {

    public static BookArticleInfoResponse from(BookArticle bookArticle) {
        return new BookArticleInfoResponse(
                bookArticle.getBook().getId(),
                bookArticle.getBook().getMainArticle().getTitle()
//                bookArticle.getPageIndex()
        );
    }
}
