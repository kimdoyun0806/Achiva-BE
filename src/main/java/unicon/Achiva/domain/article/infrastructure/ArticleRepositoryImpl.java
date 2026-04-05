package unicon.Achiva.domain.article.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import unicon.Achiva.domain.article.dto.SearchArticleCondition;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.article.entity.Question;
import unicon.Achiva.domain.book.entity.Book;
import unicon.Achiva.domain.category.Category;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final EntityManager em;

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static List<Order> toOrders(Sort sort, Root<Article> a, CriteriaBuilder cb) {
        List<Order> list = new ArrayList<>();
        if (sort == null || sort.isUnsorted()) return list;

        for (Sort.Order o : sort) {
            Path<?> path;
            switch (o.getProperty()) {
                case "createdAt" -> path = a.get("createdAt");
                case "id" -> path = a.get("id");
                case "title" -> path = a.get("title");
                case "authorCategorySeq" -> path = a.get("authorCategorySeq");
                case "category" -> path = a.get("category");
                default -> {
                    continue;
                }
            }
            list.add(o.isAscending() ? cb.asc(path) : cb.desc(path));
        }
        return list;
    }

    @Override
    public Page<Article> searchByCondition(SearchArticleCondition condition, Pageable pageable) {
        String kw = trimToNull(condition.getKeyword());
        Category category = Category.fromDisplayName(condition.getCategory());

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Article> cq = cb.createQuery(Article.class);
        Root<Article> a = cq.from(Article.class);
        Join<Article, Question> q = a.join("questions", JoinType.LEFT);

        List<Predicate> preds = new ArrayList<>();

        preds.add(cb.isFalse(a.get("isDeleted")));

        Subquery<java.util.UUID> mainArticleSubquery = cq.subquery(java.util.UUID.class);
        Root<Book> bookRoot = mainArticleSubquery.from(Book.class);
        mainArticleSubquery.select(bookRoot.get("mainArticle").get("id"));
        preds.add(cb.not(a.get("id").in(mainArticleSubquery)));

        // 카테고리 필터 (옵션)
        if (category != null) {
            preds.add(cb.equal(a.get("category"), category));
        }

        // 키워드 필터: Article.title OR Question.title OR Question.content
        if (kw != null) {
            String likeLower = "%" + kw.toLowerCase() + "%";
            String likeRaw = "%" + kw + "%";

            preds.add(cb.or(
                    cb.like(cb.lower(a.get("title")), likeLower),
                    cb.like(cb.lower(q.get("title")), likeLower),
                    cb.like(cb.coalesce(q.get("content"), ""), likeRaw)
            ));
        }

        if (!preds.isEmpty()) {
            cq.where(cb.and(preds.toArray(new Predicate[0])));
        }

        // 중복 제거 (질문 LEFT JOIN으로 인해)
        cq.select(a).distinct(true);

        List<Order> orders = toOrders(pageable.getSort(), a, cb);
        if (orders.isEmpty()) {
            orders = List.of(cb.desc(a.get("createdAt")), cb.desc(a.get("id")));
        }
        cq.orderBy(orders);

        TypedQuery<Article> contentQuery = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<Article> content = contentQuery.getResultList();

        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Article> ca = countCq.from(Article.class);
        Join<Article, Question> cqJoin = ca.join("questions", JoinType.LEFT);

        List<Predicate> countPreds = new ArrayList<>();
        countPreds.add(cb.isFalse(ca.get("isDeleted")));

        Subquery<java.util.UUID> countMainArticleSubquery = countCq.subquery(java.util.UUID.class);
        Root<Book> countBookRoot = countMainArticleSubquery.from(Book.class);
        countMainArticleSubquery.select(countBookRoot.get("mainArticle").get("id"));
        countPreds.add(cb.not(ca.get("id").in(countMainArticleSubquery)));

        if (category != null) {
            countPreds.add(cb.equal(ca.get("category"), category));
        }
        if (kw != null) {
            String likeLower = "%" + kw.toLowerCase() + "%";
            String likeRaw = "%" + kw + "%";

            countPreds.add(cb.or(
                    cb.like(cb.lower(ca.get("title")), likeLower),
                    cb.like(cb.lower(cqJoin.get("title")), likeLower),
                    cb.like(cb.coalesce(cqJoin.get("content"), ""), likeRaw)
            ));
        }
        if (!countPreds.isEmpty()) {
            countCq.where(cb.and(countPreds.toArray(new Predicate[0])));
        }
        countCq.select(cb.countDistinct(ca));

        long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
