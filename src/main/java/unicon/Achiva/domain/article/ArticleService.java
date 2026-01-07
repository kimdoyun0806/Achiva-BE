package unicon.Achiva.domain.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.article.dto.ArticleRequest;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.article.dto.ArticleWithBookResponse;
import unicon.Achiva.domain.article.dto.SearchArticleCondition;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.article.infrastructure.ArticleRepository;
import unicon.Achiva.domain.book.entity.BookArticle;
import unicon.Achiva.domain.book.infrastructure.BookArticleRepository;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.category.CategoryCountResponse;
import unicon.Achiva.domain.cheering.infrastructure.CheeringRepository;
import unicon.Achiva.domain.friendship.FriendshipStatus;
import unicon.Achiva.domain.friendship.infrastructure.FriendshipRepository;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.entity.MemberCategoryCounter;
import unicon.Achiva.domain.member.entity.MemberCategoryKey;
import unicon.Achiva.domain.member.infrastructure.CounterHelper;
import unicon.Achiva.domain.member.infrastructure.MemberCategoryCounterRepository;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.global.response.GeneralException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final CounterHelper counterHelper;

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;
    private final CheeringRepository cheeringRepository;
    private final MemberCategoryCounterRepository memberCategoryCounterRepository;
    private final BookArticleRepository bookArticleRepository;


    @Transactional(readOnly = true)
    public List<BookArticle> getBookArticleList(UUID articleId) {
        return bookArticleRepository.findBookInfosByArticleId(articleId).orElse(new ArrayList<>());
    }

    @Transactional
    public Article createArticleEntity(ArticleRequest request, UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        Category cat = request.category();

        MemberCategoryCounter dst = counterHelper.lockOrInit(memberId, cat);
        long newSeq = dst.getSize() + 1;
        dst.setSize(newSeq);

        Article article = Article.builder()
                .photoUrl(request.photoUrl())
                .title(request.title())
                .category(request.category())
                .questions(request.question().stream()
                        .map(ArticleRequest.QuestionDTO::toEntity)
                        .collect(Collectors.toList()))
                .member(member)
                .authorCategorySeq(newSeq)
                .backgroundColor(request.backgroundColor())
                .build();

        article.getQuestions().forEach(q -> q.setArticle(article));

        articleRepository.save(article);

        return article;
    }

    @Transactional
    public ArticleWithBookResponse createArticle(ArticleRequest request, UUID memberId) {
        Article article = createArticleEntity(request, memberId);
        return ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId()));
    }

    @Transactional
    public ArticleResponse updateArticle(ArticleRequest request, UUID articleId, UUID memberId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        if (!article.getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.UNAUTHORIZED_MEMBER);
        }

        Category oldCat = article.getCategory();
        long oldSeq = article.getAuthorCategorySeq();
        Category newCat = request.category();

        if (oldCat.equals(newCat)) {
            // 카테고리 동일 → 내용만 갱신 (densify 불필요)
            article.update(request);
            return ArticleResponse.fromEntity(article);
        }

        // 1) 두 그룹 락을 항상 같은 순서로 획득 (교착 방지)
        List<MemberCategoryKey> order = counterHelper.orderedKeys(memberId, oldCat, newCat);
        MemberCategoryCounter first = memberCategoryCounterRepository.lockById(order.get(0))
                .orElseGet(() -> init(order.getFirst()));
        MemberCategoryCounter second = memberCategoryCounterRepository.lockById(order.get(1))
                .orElseGet(() -> init(order.get(1)));

        // 실제로 사용할 src/dst 매핑
        MemberCategoryCounter src = oldCat.equals(first.getId().getCategory()) ? first : second;
        MemberCategoryCounter dst = newCat.equals(first.getId().getCategory()) ? first : second;

        // 2) 출발 그룹 densify (oldSeq 뒤 모두 -1), src.size--
        articleRepository.shiftLeft(memberId, oldCat, oldSeq);
        src.setSize(src.getSize() - 1);

        // 3) 도착 그룹 새 번호 = dst.size + 1, dst.size++
        long newSeq = dst.getSize() + 1;
        dst.setSize(newSeq);

        // 4) 글에 반영 + 내용 갱신
        article.changeCategoryAndSeq(newCat, newSeq);

        article.update(request);

        return ArticleResponse.fromEntity(article);
    }

    // 하드 딜리트
    @Transactional
    public void deleteArticle(UUID articleId, UUID memberId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        if (!article.getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.UNAUTHORIZED_MEMBER);
        }

        Category cat = article.getCategory();
        long seq = article.getAuthorCategorySeq();

        // 1) 해당 그룹 락
        MemberCategoryCounter counter = counterHelper.lockOrInit(memberId, cat);

        // 2) seq 뒤 모두 -1, size--
        articleRepository.shiftLeft(memberId, cat, seq);
        counter.setSize(counter.getSize() - 1);

        articleRepository.delete(article);
    }

    public ArticleResponse getArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        return ArticleResponse.fromEntity(article);
    }

    public Page<ArticleWithBookResponse> getArticles(SearchArticleCondition condition, Pageable pageable) {
        return articleRepository.searchByCondition(condition, pageable)
                .map(article -> ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId())));
    }

    public Page<ArticleWithBookResponse> getArticlesByMember(UUID memberId, Pageable pageable) {
        return articleRepository.findAllByMemberId(memberId, pageable)
                .map(article -> ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId())));
    }

    public CategoryCountResponse getArticleCountByCategory(UUID memberId) {
        List<Object[]> result = articleRepository.countArticlesByCategoryForMember(memberId);

        // 결과를 Map으로 변환 (key: Category, value: Long)
        Map<Category, Long> categoryCountMap = result.stream()
                .collect(Collectors.toMap(
                        row -> (Category) row[0],
                        row -> (Long) row[1]
                ));

        // 모든 카테고리를 순회하면서 없는 건 0L로 추가
        for (Category category : Category.values()) {
            categoryCountMap.putIfAbsent(category, 0L);
        }

        // 다시 List<Object[]> 형태로 변환하면서 getDisplayName 적용
        List<Object[]> completeResult = categoryCountMap.entrySet().stream()
                .map(entry -> new Object[]{
                        Category.getDisplayName(entry.getKey()),
                        entry.getValue()
                })
                .collect(Collectors.toList());

        return CategoryCountResponse.fromObjectList(completeResult);
    }

    public Page<ArticleWithBookResponse> getHomeArticles(UUID myId, Pageable pageable) {
        List<UUID> friendIds = friendshipRepository.findFriendIdsOf(myId, FriendshipStatus.ACCEPTED);
        List<UUID> cheererIds = cheeringRepository.findDistinctCheererIdsWhoCheeredMyArticles(myId);

        if ((friendIds == null || friendIds.isEmpty()) && (cheererIds == null || cheererIds.isEmpty())) {
            return Page.empty(pageable);
        }

        Set<UUID> friendSet = new HashSet<>(friendIds);
        List<UUID> cheererOnly = cheererIds.stream()
                .filter(id -> !friendSet.contains(id))
                .toList();

        Page<Article> page = articleRepository.findCombinedFeed(friendIds, cheererOnly, pageable);
        return page.map(article -> ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId())));
    }


    // 게시글 카테고리 순서 관련 메서드 by GPT
    // by GPT 코드를 눈물을 흘리며 수정하는 사람 남김
    @Transactional
    public void moveCategory(UUID articleId, Category newCategory) {
        Article a = articleRepository.findById(articleId).orElseThrow();

        Category src = a.getCategory();
        if (src == newCategory) return;

        UUID memberId = a.getMember().getId();
        long oldSeq = a.getAuthorCategorySeq();

        // 1) 락 순서 고정 (교착 방지): 키를 문자열로 비교해 작은 쪽 먼저
        MemberCategoryKey k1 = new MemberCategoryKey(memberId, min(src, newCategory));
        MemberCategoryKey k2 = new MemberCategoryKey(memberId, max(src, newCategory));

        MemberCategoryCounter first = memberCategoryCounterRepository.lockById(k1).orElseGet(() -> initCounter(k1));
        MemberCategoryCounter second = memberCategoryCounterRepository.lockById(k2).orElseGet(() -> initCounter(k2));

        // 2) 출발 그룹 densify: oldSeq 뒤 모두 -1, size--
        articleRepository.shiftLeft(memberId, src, oldSeq);
        if (src.equals(k1.getCategory())) first.setSize(first.getSize() - 1);
        else second.setSize(second.getSize() - 1);

        // 3) 도착 그룹 새 번호 = size + 1, size++
        MemberCategoryCounter dstCounter = newCategory.equals(k1.getCategory()) ? first : second;
        long newSeq = dstCounter.getSize() + 1;
        dstCounter.setSize(newSeq); // size++ (newSeq == oldSize+1)

        // 4) 글 갱신
        a.changeCategoryAndSeq(newCategory, newSeq);
    }

    public Page<ArticleWithBookResponse> getMemberInterestFeed(UUID memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));


        List<Category> cats = Optional.ofNullable(member.getCategories()).orElseGet(Collections::emptyList);
        if (cats.isEmpty()) {
            return Page.empty(pageable);
        }

        // 기본 정렬 보정: createdAt DESC
        Pageable sorted = pageable;
        if (pageable.getSort().isUnsorted()) {
            sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Article> page = articleRepository.findByCategoryIn(cats, sorted);
        return page.map(article -> ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId())));
    }

    private MemberCategoryCounter initCounter(MemberCategoryKey key) {
        MemberCategoryCounter c = new MemberCategoryCounter();
        c.setId(key);
        c.setSize(0L);
        return memberCategoryCounterRepository.saveAndFlush(c);
    }

    private Category min(Category a, Category b) {
        return a.name().compareTo(b.name()) <= 0 ? a : b;
    }

    private Category max(Category a, Category b) {
        return a.name().compareTo(b.name()) >= 0 ? a : b;
    }

    private MemberCategoryCounter init(MemberCategoryKey key) {
        MemberCategoryCounter c = new MemberCategoryCounter();
        c.setId(key);
        c.setSize(0L);
        return memberCategoryCounterRepository.saveAndFlush(c);
    }

    public Page<ArticleWithBookResponse> getArticlesByMemberAndCateogry(UUID memberId, String category, Pageable pageable) {
        return articleRepository.findByMemberIdWithCategory(memberId, Category.fromDisplayName(category), pageable)
                .map(article -> ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId())));
    }

    public Page<ArticleWithBookResponse> getAllArticlesFeed(Pageable pageable) {
        // 기본 정렬 보정: createdAt DESC
        Pageable sorted = pageable;
        if (pageable.getSort().isUnsorted()) {
            sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Article> page = articleRepository.findAllByIsDeletedFalse(sorted);
        return page.map(article -> ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId())));
    }

    public Page<ArticleWithBookResponse> getCheeringRelatedArticlesFeed(UUID memberId, Pageable pageable) {
        // 기본 정렬 보정
        Pageable sorted = pageable;
        if (pageable.getSort().isUnsorted()) {
            sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // 응원 관계 사용자들의 게시글 조회
        Page<Article> page = articleRepository.findByCheeringRelatedMembers(memberId, sorted);

        // ArticleWithBookResponse로 변환
        return page.map(article -> ArticleWithBookResponse.fromEntity(article, getBookArticleList(article.getId())));
    }
}