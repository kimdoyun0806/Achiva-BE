package unicon.Achiva.domain.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.article.dto.ArticleRequest;
import unicon.Achiva.domain.article.dto.ArticleCountResponse;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.article.dto.ArticleWithBookResponse;
import unicon.Achiva.domain.article.dto.SearchArticleCondition;
import unicon.Achiva.domain.article.dto.TotalCharacterCountResponse;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.article.entity.ArticlePushHistory;
import unicon.Achiva.domain.article.infrastructure.ArticlePushHistoryRepository;
import unicon.Achiva.domain.article.infrastructure.ArticleRepository;
import unicon.Achiva.domain.book.entity.BookArticle;
import unicon.Achiva.domain.book.infrastructure.BookArticleRepository;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.category.CategoryCharacterCountResponse;
import unicon.Achiva.domain.category.CategoryCountResponse;
import unicon.Achiva.domain.category.CategoryRankingResponse;
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
import unicon.Achiva.domain.moim.entity.MoimScore;
import unicon.Achiva.domain.moim.repository.MoimScoreRepository;
import unicon.Achiva.domain.push.PushService;
import unicon.Achiva.domain.push.dto.PushSendRequest;
import unicon.Achiva.global.response.GeneralException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final PushService pushService;
    private final ArticlePushHistoryRepository articlePushHistoryRepository;
    private final MoimScoreRepository moimScoreRepository;


    @Transactional(readOnly = true)
    public List<BookArticle> getBookArticleList(UUID articleId) {
        return bookArticleRepository.findBookInfosByArticleId(articleId).orElse(new ArrayList<>());
    }

    @Transactional
    public Article createArticleEntity(ArticleRequest request, UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        Category cat = request.category();
        LocalDateTime createdAt = LocalDateTime.now();
        ArticleStatsSnapshot statsSnapshot = calculateNewArticleStats(memberId, createdAt);

        MemberCategoryCounter dst = counterHelper.lockOrInit(memberId, cat);
        long newSeq = dst.getSize() + 1;
        dst.setSize(newSeq);

        Article article = Article.builder()
                .photoUrls(request.photoUrls() != null ? new ArrayList<>(request.photoUrls()) : new ArrayList<>())
                .title(request.title())
                .category(request.category())
                .questions(request.question().stream()
                        .map(ArticleRequest.QuestionDTO::toEntity)
                .collect(Collectors.toList()))
                .member(member)
                .authorCategorySeq(newSeq)
                .backgroundColor(request.backgroundColor())
                .weeklyWorkoutCount(statsSnapshot.weeklyWorkoutCount())
                .continuousGoalWeeks(statsSnapshot.continuousGoalWeeks())
                .build();

        article.getQuestions().forEach(q -> q.setArticle(article));

        articleRepository.save(article);
        increaseMoimScores(article);

        return article;
    }

    @Transactional
    public ArticleWithBookResponse createArticle(ArticleRequest request, UUID memberId) {
        Article article = createArticleEntity(request, memberId);

        // 게시글 생성 후 친구들에게 푸시 알림 전송
        sendFriendWorkoutPushNotifications(article);

        return toArticleWithBookResponse(article, getMemberArticleCountMap(List.of(memberId)));
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

        decreaseMoimScores(article);
        articleRepository.delete(article);
    }

    private void increaseMoimScores(Article article) {
        List<MoimScore> activeScores = moimScoreRepository.findActiveScoresByMemberId(article.getMember().getId());
        for (MoimScore moimScore : activeScores) {
            moimScore.increaseScore();
            moimScore.getMoim().increaseScore();
        }
    }

    private void decreaseMoimScores(Article article) {
        LocalDateTime articleCreatedAt = Optional.ofNullable(article.getCreatedAt()).orElse(LocalDateTime.now());
        List<MoimScore> targetScores = moimScoreRepository.findScoresContainingArticleCreatedAt(
                article.getMember().getId(),
                articleCreatedAt
        );

        for (MoimScore moimScore : targetScores) {
            moimScore.decreaseScore();
            moimScore.getMoim().decreaseScore();
        }
    }

    public ArticleResponse getArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        return ArticleResponse.fromEntity(article);
    }

    public Page<ArticleWithBookResponse> getArticles(SearchArticleCondition condition, Pageable pageable) {
        return toArticleWithBookResponsePage(articleRepository.searchByCondition(condition, pageable));
    }

    public Page<ArticleWithBookResponse> getArticlesByMember(UUID memberId, Pageable pageable) {
        return toArticleWithBookResponsePage(articleRepository.findAllByMemberId(memberId, pageable));
    }

    public CategoryCountResponse getArticleCountByCategory(UUID memberId) {
        List<Object[]> result = articleRepository.countArticlesByCategoryForMember(memberId);
        return toCategoryCountResponse(result);
    }

    public CategoryCountResponse getWeeklyArticleCountByCategory(UUID memberId) {
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        List<Object[]> result = articleRepository.countArticlesByCategoryForMemberAndDateRange(
                memberId,
                weekStart,
                null
        );

        return toCategoryCountResponse(result);
    }

    private CategoryCountResponse toCategoryCountResponse(List<Object[]> result) {
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

    public unicon.Achiva.domain.member.dto.MemberStatsResponse getMemberStats(UUID memberId) {
        List<LocalDateTime> createdDates = articleRepository.findAllCreatedAtByMemberId(memberId);
        ArticleStatsSnapshot statsSnapshot = calculateStatsSnapshot(createdDates, LocalDate.now());
        return new unicon.Achiva.domain.member.dto.MemberStatsResponse(
                statsSnapshot.weeklyWorkoutCount(),
                statsSnapshot.continuousGoalWeeks()
        );
    }

    public Map<UUID, unicon.Achiva.domain.member.dto.MemberStatsResponse> getMemberStatsMap(Collection<UUID> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        LocalDate referenceDate = LocalDate.now();
        Map<UUID, List<LocalDateTime>> createdDatesByMemberId = new HashMap<>();
        for (Object[] row : articleRepository.findAllCreatedAtByMemberIds(memberIds)) {
            UUID memberId = (UUID) row[0];
            LocalDateTime createdAt = (LocalDateTime) row[1];
            createdDatesByMemberId.computeIfAbsent(memberId, ignored -> new ArrayList<>()).add(createdAt);
        }

        Map<UUID, unicon.Achiva.domain.member.dto.MemberStatsResponse> statsMap = new HashMap<>();
        for (UUID memberId : memberIds) {
            ArticleStatsSnapshot statsSnapshot = calculateStatsSnapshot(
                    createdDatesByMemberId.getOrDefault(memberId, Collections.emptyList()),
                    referenceDate
            );
            statsMap.put(memberId, new unicon.Achiva.domain.member.dto.MemberStatsResponse(
                    statsSnapshot.weeklyWorkoutCount(),
                    statsSnapshot.continuousGoalWeeks()
            ));
        }

        return statsMap;
    }

    private ArticleStatsSnapshot calculateNewArticleStats(UUID memberId, LocalDateTime createdAt) {
        List<LocalDateTime> createdDates = new ArrayList<>(articleRepository.findAllCreatedAtByMemberId(memberId));
        createdDates.add(createdAt);
        return calculateStatsSnapshot(createdDates, createdAt.toLocalDate());
    }

    private ArticleStatsSnapshot calculateStatsSnapshot(List<LocalDateTime> createdDates, LocalDate referenceDate) {
        LocalDate weekStart = referenceDate.with(DayOfWeek.MONDAY);
        LocalDate nextWeekStart = weekStart.plusWeeks(1);

        int weeklyWorkoutCount = (int) createdDates.stream()
                .map(LocalDateTime::toLocalDate)
                .filter(date -> !date.isBefore(weekStart) && date.isBefore(nextWeekStart))
                .count();

        return new ArticleStatsSnapshot(
                weeklyWorkoutCount,
                calculateContinuousGoalWeeks(createdDates, referenceDate)
        );
    }

    private int calculateContinuousGoalWeeks(List<LocalDateTime> createdDates, LocalDate referenceDate) {
        Map<LocalDate, Set<LocalDate>> activeDaysByWeek = new HashMap<>();

        for (LocalDateTime createdDate : createdDates) {
            LocalDate date = createdDate.toLocalDate();
            LocalDate mondayAnchor = date.with(DayOfWeek.MONDAY);
            activeDaysByWeek.computeIfAbsent(mondayAnchor, ignored -> new HashSet<>()).add(date);
        }

        int continuousWeeks = 0;
        LocalDate checkDate = referenceDate;
        boolean isCurrentWeek = true;

        while (true) {
            LocalDate mondayAnchor = checkDate.with(DayOfWeek.MONDAY);
            int activeDayCount = activeDaysByWeek.getOrDefault(mondayAnchor, Collections.emptySet()).size();

            if (activeDayCount >= 3) {
                continuousWeeks++;
                isCurrentWeek = false;
            } else {
                if (isCurrentWeek) {
                    isCurrentWeek = false;
                } else {
                    break;
                }
            }

            checkDate = checkDate.minusWeeks(1);
        }

        return continuousWeeks;
    }

    private record ArticleStatsSnapshot(int weeklyWorkoutCount, int continuousGoalWeeks) {
    }

    private Page<ArticleWithBookResponse> toArticleWithBookResponsePage(Page<Article> page) {
        Map<UUID, Long> memberArticleCountMap = getMemberArticleCountMap(
                page.getContent().stream()
                        .map(article -> article.getMember().getId())
                        .distinct()
                        .toList()
        );

        List<ArticleWithBookResponse> content = page.getContent().stream()
                .map(article -> toArticleWithBookResponse(article, memberArticleCountMap))
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    private ArticleWithBookResponse toArticleWithBookResponse(Article article, Map<UUID, Long> memberArticleCountMap) {
        long memberArticleCount = memberArticleCountMap.getOrDefault(article.getMember().getId(), 0L);
        return ArticleWithBookResponse.fromEntity(
                article,
                getBookArticleList(article.getId()),
                memberArticleCount
        );
    }

    private Map<UUID, Long> getMemberArticleCountMap(List<UUID> memberIds) {
        if (memberIds.isEmpty()) {
            return Map.of();
        }

        return articleRepository.countArticlesByMemberIds(memberIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
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
        return toArticleWithBookResponsePage(page);
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
        return toArticleWithBookResponsePage(
                articleRepository.findByMemberIdWithCategory(memberId, Category.fromDisplayName(category), pageable)
        );
    }

    public Page<ArticleWithBookResponse> getAllArticlesFeed(Pageable pageable) {
        // 기본 정렬 보정: createdAt DESC
        Pageable sorted = pageable;
        if (pageable.getSort().isUnsorted()) {
            sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Article> page = articleRepository.findAllByIsDeletedFalse(sorted);
        return toArticleWithBookResponsePage(page);
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
        return toArticleWithBookResponsePage(page);
    }

    public TotalCharacterCountResponse getTotalCharacterCountByDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        long totalCount = articleRepository.countTotalCharactersByDateRange(memberId, startDate, endDate);
        return new TotalCharacterCountResponse(totalCount);
    }

    public ArticleCountResponse getArticleCountByDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        long articleCount = articleRepository.countArticlesByDateRange(memberId, startDate, endDate);
        return new ArticleCountResponse(articleCount);
    }

    public CategoryCharacterCountResponse getCharacterCountByCategory(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> result = articleRepository.countCharactersByCategoryAndDateRange(memberId, startDate, endDate);

        // 결과를 Map으로 변환 (key: Category, value: Long)
        Map<Category, Long> categoryCharacterCountMap = result.stream()
                .collect(Collectors.toMap(
                        row -> (Category) row[0],
                        row -> (Long) row[1]
                ));

        // 모든 카테고리를 순회하면서 없는 건 0L로 추가
        for (Category category : Category.values()) {
            categoryCharacterCountMap.putIfAbsent(category, 0L);
        }

        // 다시 List<Object[]> 형태로 변환하면서 getDisplayName 적용
        List<Object[]> completeResult = categoryCharacterCountMap.entrySet().stream()
                .map(entry -> new Object[]{
                        Category.getDisplayName(entry.getKey()),
                        entry.getValue()
                })
                .collect(Collectors.toList());

        return CategoryCharacterCountResponse.fromObjectList(completeResult);
    }

    public CategoryRankingResponse getCategoryRanking() {
        Map<Category, List<CategoryRankingResponse.CategoryRankingMember>> rankingMap = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            rankingMap.put(category, new ArrayList<>());
        }

        for (Object[] row : articleRepository.countArticlesByCategoryAndMember()) {
            Category category = (Category) row[0];
            UUID memberId = (UUID) row[1];
            String nickName = (String) row[2];
            String profileImageUrl = (String) row[3];
            Long articleCount = (Long) row[4];

            rankingMap.get(category).add(new CategoryRankingResponse.CategoryRankingMember(
                    memberId,
                    nickName,
                    profileImageUrl,
                    articleCount
            ));
        }

        List<CategoryRankingResponse.CategoryRanking> categories = rankingMap.entrySet().stream()
                .map(entry -> new CategoryRankingResponse.CategoryRanking(
                        Category.getDisplayName(entry.getKey()),
                        entry.getValue()
                ))
                .toList();

        return new CategoryRankingResponse(categories);
    }

    /**
     * 친구들에게 운동 게시글 푸시 알림 전송
     * PDF 요구사항:
     * - 친구 중 친구 간 푸쉬알림 동의 유저에게만 발송
     * - Title: "{닉네임}님이 오늘 운동했어요 💪"
     * - Body: "나는 오늘 운동했다. 다음은 네 차례야 🔥"
     * - 제외 대상: 본인, 전체 푸시 OFF, 친구 운동 알림 OFF, 개별 설정 OFF
     * - 발송 정책: 동일 작성자가 하루에 여러 게시글 올릴 경우 수신자당 하루 1회만 발송
     */
    private void sendFriendWorkoutPushNotifications(Article article) {
        try {
            UUID authorId = article.getMember().getId();
            String authorNickname = article.getMember().getNickName();
            LocalDate today = LocalDate.now();

            // 1. 작성자의 모든 친구 관계 조회 (양방향)
            List<unicon.Achiva.domain.friendship.entity.Friendship> friendships =
                    friendshipRepository.findAllAcceptedFriendships(authorId, FriendshipStatus.ACCEPTED);

            // 2. 필터링 및 푸시 전송
            friendships.forEach(friendship -> {
                try {
                    // 작성자가 아닌 쪽이 수신자
                    Member friendMember;
                    boolean allowsPostPush;

                    if (friendship.getRequester().getId().equals(authorId)) {
                        // 작성자가 requester -> receiver가 친구
                        friendMember = friendship.getReceiver();
                        allowsPostPush = friendship.isReceiverAllowsPostPush();
                    } else {
                        // 작성자가 receiver -> requester가 친구
                        friendMember = friendship.getRequester();
                        allowsPostPush = friendship.isRequesterAllowsPostPush();
                    }

                    UUID receiverId = friendMember.getId();

                    // 전체 푸시 OFF 확인
                    if (!friendMember.isPushEnabled()) {
                        log.debug("[Article] 전체 푸시 비활성화 - friendId: {}", receiverId);
                        return;
                    }

                    // 친구 운동 알림 OFF 확인
                    if (!friendMember.isFriendWorkoutPushEnabled()) {
                        log.debug("[Article] 친구 운동 푸시 비활성화 - friendId: {}", receiverId);
                        return;
                    }

                    // 개별 설정 OFF 확인
                    if (!allowsPostPush) {
                        log.debug("[Article] 개별 게시글 푸시 비활성화 - friendId: {}", receiverId);
                        return;
                    }

                    // 하루 1회 발송 정책: 오늘 이미 보냈는지 확인
                    boolean alreadySentToday = articlePushHistoryRepository
                            .existsByAuthorIdAndReceiverIdAndPushDate(authorId, receiverId, today);

                    if (alreadySentToday) {
                        log.debug("[Article] 오늘 이미 푸시 전송함 - authorId: {}, receiverId: {}", authorId, receiverId);
                        return;
                    }

                    String title = String.format("%s님이 오늘 운동했어요 💪", authorNickname);
                    String body = "나는 오늘 운동했다. 다음은 네 차례야 🔥";

                    Map<String, Object> data = new HashMap<>();
                    data.put("type", "friend_workout_post");
                    data.put("articleId", article.getId().toString());
                    data.put("fromUserId", authorId.toString());

                    PushSendRequest pushRequest = PushSendRequest.builder()
                            .targetMemberId(receiverId)
                            .title(title)
                            .body(body)
                            .data(data)
                            .build();

                    pushService.sendPushNotification(authorId, pushRequest);

                    // 푸시 발송 이력 저장
                    ArticlePushHistory history = ArticlePushHistory.builder()
                            .authorId(authorId)
                            .receiverId(receiverId)
                            .pushDate(today)
                            .articleId(article.getId())
                            .build();
                    articlePushHistoryRepository.save(history);

                    log.info("[Article] 친구 운동 게시글 푸시 알림 전송 성공 - from: {}, to: {}, articleId: {}",
                            authorId, receiverId, article.getId());
                } catch (Exception e) {
                    log.error("[Article] 친구 운동 게시글 푸시 알림 전송 실패 - error: {}", e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            // 푸시 전송 실패 시 로그만 남기고 비즈니스 로직은 계속 진행
            log.error("[Article] 친구 운동 게시글 푸시 알림 전송 중 오류 - articleId: {}, error: {}",
                    article.getId(), e.getMessage(), e);
        }
    }
}
