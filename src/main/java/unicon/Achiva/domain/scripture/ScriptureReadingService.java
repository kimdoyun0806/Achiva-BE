package unicon.Achiva.domain.scripture;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.article.ArticleService;
import unicon.Achiva.domain.article.dto.ArticleRequest;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.domain.organization.OrganizationAccessService;
import unicon.Achiva.domain.scripture.dto.ScriptureCalendarItemResponse;
import unicon.Achiva.domain.scripture.dto.ScriptureCalendarResponse;
import unicon.Achiva.domain.scripture.dto.ScriptureProgressItemResponse;
import unicon.Achiva.domain.scripture.dto.ScriptureProgressListResponse;
import unicon.Achiva.domain.scripture.dto.ScriptureProgressUpdateRequest;
import unicon.Achiva.domain.scripture.dto.ScriptureReadingArticleCreateRequest;
import unicon.Achiva.domain.scripture.entity.ArticleScriptureReading;
import unicon.Achiva.domain.scripture.entity.MemberScriptureProgress;
import unicon.Achiva.domain.scripture.infrastructure.ArticleScriptureReadingRepository;
import unicon.Achiva.domain.scripture.infrastructure.MemberScriptureProgressRepository;
import unicon.Achiva.global.response.GeneralException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScriptureReadingService {

    private static final String SCRIPTURE_CONTENT_QUESTION_TITLE = "성경 일독 본문";

    private final MemberRepository memberRepository;
    private final MemberScriptureProgressRepository memberScriptureProgressRepository;
    private final ArticleScriptureReadingRepository articleScriptureReadingRepository;
    private final ArticleService articleService;
    private final OrganizationAccessService organizationAccessService;

    public ScriptureProgressListResponse getMyProgress(UUID memberId) {
        List<ScriptureProgressItemResponse> items = memberScriptureProgressRepository
                .findAllByMemberIdAndIsDeletedFalseOrderByUpdatedAtDesc(memberId)
                .stream()
                .filter(progress -> progress.getCompletedChapters() != null && progress.getCompletedChapters() > 0)
                .map(ScriptureProgressItemResponse::fromEntity)
                .toList();

        return ScriptureProgressListResponse.builder()
                .items(items)
                .build();
    }

    @Transactional
    public ScriptureProgressItemResponse upsertProgress(UUID memberId, String scriptureId, ScriptureProgressUpdateRequest request) {
        validateProgress(scriptureId, request.completedChapters());

        MemberScriptureProgress progress = memberScriptureProgressRepository
                .findByMemberIdAndScriptureIdAndIsDeletedFalse(memberId, scriptureId)
                .orElseGet(() -> MemberScriptureProgress.builder()
                        .member(getMember(memberId))
                        .scriptureId(scriptureId)
                        .completedChapters(request.completedChapters())
                        .build());

        progress.updateCompletedChapters(request.completedChapters());

        MemberScriptureProgress saved = memberScriptureProgressRepository.save(progress);
        return ScriptureProgressItemResponse.fromEntity(saved);
    }

    @Transactional
    public ArticleResponse createScriptureArticle(UUID memberId, ScriptureReadingArticleCreateRequest request) {
        validateReading(request.scriptureId(), request.startChapter(), request.endChapter(), request.completedChapters());

        ArticleRequest articleRequest = new ArticleRequest(
                List.of(),
                buildTitle(request.scriptureId(), request.startChapter(), request.endChapter()),
                Category.BIBLE,
                List.of(new ArticleRequest.QuestionDTO(
                        SCRIPTURE_CONTENT_QUESTION_TITLE,
                        request.content().trim()
                )),
                null
        );

        Article article = articleService.createArticleEntity(articleRequest, memberId);

        LocalDate readAt = request.readAt() != null
                ? request.readAt()
                : resolveReadAtFromCreatedAt(article.getCreatedAt());

        ArticleScriptureReading scriptureReading = ArticleScriptureReading.create(
                article,
                request.scriptureId(),
                request.startChapter(),
                request.endChapter(),
                request.completedChapters(),
                readAt
        );

        article.setScriptureReading(scriptureReading);
        articleScriptureReadingRepository.save(scriptureReading);
        articleService.notifyArticleCreated(article);

        return ArticleResponse.fromEntity(article);
    }

    public ScriptureCalendarResponse getCalendar(UUID requesterId, UUID targetMemberId, String yearMonth) {
        UUID memberId = organizationAccessService.getAccessibleMember(requesterId, targetMemberId).getId();
        YearMonth parsed = parseYearMonth(yearMonth);

        List<ScriptureCalendarItemResponse> items = articleScriptureReadingRepository
                .findCalendarItemsByMemberId(memberId, parsed.atDay(1), parsed.atEndOfMonth())
                .stream()
                .map(entity -> ScriptureCalendarItemResponse.fromEntity(
                        entity,
                        ScriptureCalendarItemResponse.extractContent(entity)
                ))
                .toList();

        return ScriptureCalendarResponse.builder()
                .yearMonth(parsed.toString())
                .items(items)
                .build();
    }

    private Member getMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(unicon.Achiva.domain.member.MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateProgress(String scriptureId, Integer completedChapters) {
        int totalChapters = validateScripture(scriptureId);

        if (completedChapters == null || completedChapters < 0) {
            throw new GeneralException(ScriptureErrorCode.INVALID_COMPLETED_CHAPTERS);
        }
        if (completedChapters > totalChapters) {
            throw new GeneralException(ScriptureErrorCode.CHAPTER_OUT_OF_RANGE);
        }
    }

    private void validateReading(String scriptureId, Integer startChapter, Integer endChapter, Integer completedChapters) {
        int totalChapters = validateScripture(scriptureId);

        if (startChapter == null || startChapter < 1) {
            throw new GeneralException(ScriptureErrorCode.INVALID_START_CHAPTER);
        }
        if (endChapter == null || endChapter < startChapter) {
            throw new GeneralException(ScriptureErrorCode.INVALID_END_CHAPTER);
        }
        if (endChapter > totalChapters) {
            throw new GeneralException(ScriptureErrorCode.CHAPTER_OUT_OF_RANGE);
        }
        if (completedChapters == null || completedChapters < 0) {
            throw new GeneralException(ScriptureErrorCode.INVALID_COMPLETED_CHAPTERS);
        }
        if (completedChapters > totalChapters) {
            throw new GeneralException(ScriptureErrorCode.CHAPTER_OUT_OF_RANGE);
        }
        if (completedChapters < endChapter) {
            throw new GeneralException(ScriptureErrorCode.COMPLETED_CHAPTERS_BEFORE_END);
        }
    }

    private int validateScripture(String scriptureId) {
        if (!ScriptureMetadataRegistry.contains(scriptureId)) {
            throw new GeneralException(ScriptureErrorCode.INVALID_SCRIPTURE_ID);
        }
        return ScriptureMetadataRegistry.totalChaptersOf(scriptureId);
    }

    private YearMonth parseYearMonth(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new GeneralException(ScriptureErrorCode.INVALID_YEAR_MONTH);
        }
    }

    private String buildTitle(String scriptureId, Integer startChapter, Integer endChapter) {
        if (startChapter.equals(endChapter)) {
            return scriptureId + " " + startChapter + "장";
        }
        return scriptureId + " " + startChapter + "장-" + endChapter + "장";
    }

    private LocalDate resolveReadAtFromCreatedAt(LocalDateTime createdAt) {
        return createdAt != null ? createdAt.toLocalDate() : LocalDate.now();
    }
}
