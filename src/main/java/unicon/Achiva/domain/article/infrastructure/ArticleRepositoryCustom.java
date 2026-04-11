package unicon.Achiva.domain.article.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unicon.Achiva.domain.article.dto.SearchArticleCondition;
import unicon.Achiva.domain.article.entity.Article;


public interface ArticleRepositoryCustom {
    Page<Article> searchByCondition(SearchArticleCondition condition, Long organizationId, Pageable pageable);
}
