package unicon.Achiva.domain.moim;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.article.infrastructure.ArticleRepository;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.domain.moim.dto.MoimCreateRequest;
import unicon.Achiva.domain.moim.dto.MoimDetailResponse;
import unicon.Achiva.domain.moim.dto.MoimResponse;
import unicon.Achiva.domain.moim.dto.MoimUpdateRequest;
import unicon.Achiva.domain.moim.entity.Moim;
import unicon.Achiva.domain.moim.entity.MoimMember;
import unicon.Achiva.domain.moim.entity.MoimRole;
import unicon.Achiva.domain.moim.entity.MoimScore;
import unicon.Achiva.domain.moim.repository.MoimMemberRepository;
import unicon.Achiva.domain.moim.repository.MoimRepository;
import unicon.Achiva.domain.moim.repository.MoimScoreRepository;
import unicon.Achiva.global.response.GeneralException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimService {

    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final MoimScoreRepository moimScoreRepository;

    @Transactional
    public MoimResponse createMoim(MoimCreateRequest request, UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        boolean isPrivate = Boolean.TRUE.equals(request.getPrivateMoim());
        String encodedPassword = null;
        if (isPrivate && request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = request.getPassword(); // 평문 저장 (임시)
        }

        Moim moim = Moim.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxMember(request.getMaxMember())
                .isPrivate(isPrivate)
                .password(encodedPassword)
                .categories(request.getCategories())
                .isOfficial(false)
                .build();

        Moim savedMoim = moimRepository.save(moim);

        MoimMember moimMember = MoimMember.builder()
                .moim(savedMoim)
                .member(member)
                .role(MoimRole.LEADER)
                .build();
                
        moimMemberRepository.save(moimMember);
        savedMoim.getMembers().add(moimMember);
        moimScoreRepository.save(MoimScore.builder()
                .moim(savedMoim)
                .member(member)
                .build());

        return MoimResponse.from(savedMoim);
    }

    public Page<MoimResponse> getMoims(String keyword, List<Category> categories, Boolean isOfficial, Pageable pageable) {
        boolean hasCategories = categories != null && !categories.isEmpty();
        Page<Moim> moims = moimRepository.findMoimsBySearchAndCategory(keyword, categories, hasCategories, isOfficial, pageable);
        return moims.map(MoimResponse::from);
    }

    public MoimDetailResponse getMoimDetail(Long moimId, UUID currentMemberId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        return buildMoimDetailResponse(moim, currentMemberId);
    }

    private MoimDetailResponse buildMoimDetailResponse(Moim moim, UUID currentMemberId) {

        // 이번 달 시작일
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();

        // 모임 멤버 UUID 목록
        List<UUID> memberIds = moim.getMembers().stream()
                .map(mm -> mm.getMember().getId())
                .collect(Collectors.toList());

        // 멤버별 이번 달 게시물 수
        Map<UUID, Long> postCountMap = new HashMap<>();
        Map<UUID, Long> scoreMap = new HashMap<>();
        Map<UUID, Long> weeklyStreakMap = new HashMap<>();

        if (!memberIds.isEmpty()) {
            scoreMap = moimScoreRepository.findByMoim_IdAndMember_IdInAndLeftAtIsNull(moim.getId(), memberIds).stream()
                    .collect(Collectors.toMap(ms -> ms.getMember().getId(), ms -> (long) ms.getScore()));
            List<Object[]> counts = articleRepository.countMonthlyPostsByMemberIds(memberIds, monthStart);
            for (Object[] row : counts) {
                UUID memberId = (UUID) row[0];
                Long count = (Long) row[1];
                postCountMap.put(memberId, count);
            }

            List<Object[]> streakCounts = articleRepository.countWeeklyActiveDaysByMemberIds(memberIds, weekStart);
            for (Object[] row : streakCounts) {
                UUID memberId = (UUID) row[0];
                Long count = (Long) row[1];
                weeklyStreakMap.put(memberId, count);
            }
        }

        return MoimDetailResponse.from(moim, currentMemberId, scoreMap, postCountMap, weeklyStreakMap);
    }

    /**
     * 이번 달 모임 멤버들이 작성한 게시물 피드
     */
    public Page<ArticleResponse> getMoimFeed(Long moimId, UUID currentMemberId, Pageable pageable) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        List<UUID> memberIds = moim.getMembers().stream()
                .map(mm -> mm.getMember().getId())
                .collect(Collectors.toList());

        if (memberIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Page<Article> articles = articleRepository.findByMemberIdsAndCreatedAtAfter(memberIds, monthStart, pageable);
        return articles.map(ArticleResponse::fromEntity);
    }

    public List<MoimResponse> getMyMoims(UUID memberId) {
        List<MoimMember> myMemberships = moimMemberRepository.findByMemberId(memberId);
        return myMemberships.stream()
                .map(MoimMember::getMoim)
                .map(MoimResponse::from)
                .toList();
    }

    @Transactional
    public void joinMoim(Long moimId, UUID memberId, String inputPassword) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (moimMemberRepository.existsByMoimIdAndMemberId(moimId, memberId)) {
            throw new GeneralException(MoimErrorCode.ALREADY_JOINED);
        }

        if (moim.getMemberCount() >= moim.getMaxMember()) {
            throw new GeneralException(MoimErrorCode.MOIM_ALREADY_FULL);
        }

        if (moim.isPrivate()) {
            if (inputPassword == null || !moim.checkPassword(inputPassword)) {
                throw new GeneralException(MoimErrorCode.INVALID_PASSWORD);
            }
        }

        MoimMember moimMember = MoimMember.builder()
                .moim(moim)
                .member(member)
                .role(MoimRole.MEMBER)
                .build();

        moimMemberRepository.save(moimMember);
        moimScoreRepository.save(MoimScore.builder()
                .moim(moim)
                .member(member)
                .build());
    }

    @Transactional
    public MoimDetailResponse updateMoimSettings(Long moimId, UUID memberId, MoimUpdateRequest request) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        boolean isLeader = moim.getMembers().stream()
                .anyMatch(mm -> mm.getMember().getId().equals(memberId) && mm.getRole() == MoimRole.LEADER);

        if (!isLeader) {
            throw new GeneralException(MoimErrorCode.UNAUTHORIZED_ACTION);
        }

        moim.update(
                request.getName(),
                request.getDescription(),
                request.getMaxMember(),
                request.getPrivateMoim(),
                request.getPassword(),
                request.getOfficialMoim(),
                request.getTargetAmount(),
                request.getPokeDays(),
                request.getCategories()
        );

        return buildMoimDetailResponse(moim, memberId);
    }

    @Transactional
    public void leaveMoim(Long moimId, UUID memberId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        MoimMember moimMember = moimMemberRepository.findByMoimIdAndMemberId(moimId, memberId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.UNAUTHORIZED_ACTION));

        List<MoimMember> allMembers = moim.getMembers();

        // 방장인 경우: 다른 멤버에게 방장 위임 후 탈퇴
        if (moimMember.getRole() == MoimRole.LEADER) {
            // 방장 외 다른 멤버 찾기
            MoimMember nextLeader = allMembers.stream()
                    .filter(mm -> !mm.getMember().getId().equals(memberId))
                    .findFirst()
                    .orElse(null);

            if (nextLeader != null) {
                // 다른 멤버에게 방장 위임
                nextLeader.promoteToLeader();
            }
            // nextLeader가 null이면 혼자 있는 모임 → 삭제 없이 그냥 탈퇴 (모임은 남음)
        }

        moimScoreRepository.findByMoim_IdAndMember_IdAndLeftAtIsNull(moimId, memberId)
                .ifPresent(score -> score.leave(LocalDateTime.now()));
        moimMemberRepository.delete(moimMember);
        moim.getMembers().remove(moimMember);
    }

    @Transactional
    public void removeMoimMember(Long moimId, UUID requesterId, UUID targetMemberId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        boolean isLeader = moim.getMembers().stream()
                .anyMatch(mm -> mm.getMember().getId().equals(requesterId) && mm.getRole() == MoimRole.LEADER);

        if (!isLeader) {
            throw new GeneralException(MoimErrorCode.UNAUTHORIZED_ACTION);
        }

        MoimMember targetMoimMember = moimMemberRepository.findByMoimIdAndMemberId(moimId, targetMemberId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_MEMBER_NOT_FOUND));

        if (targetMoimMember.getRole() == MoimRole.LEADER) {
            throw new GeneralException(MoimErrorCode.LEADER_CANNOT_BE_REMOVED);
        }

        moimScoreRepository.findByMoim_IdAndMember_IdAndLeftAtIsNull(moimId, targetMemberId)
                .ifPresent(score -> score.leave(LocalDateTime.now()));
        moimMemberRepository.delete(targetMoimMember);
        moim.getMembers().remove(targetMoimMember);
    }

    @Transactional
    public void deleteMoim(Long moimId, UUID memberId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        boolean isLeader = moim.getMembers().stream()
                .anyMatch(mm -> mm.getMember().getId().equals(memberId) && mm.getRole() == MoimRole.LEADER);

        if (!isLeader) {
            throw new GeneralException(MoimErrorCode.UNAUTHORIZED_ACTION);
        }

        // 모임 멤버 전체 삭제 후 모임 삭제
        moimScoreRepository.deleteAllByMoim_Id(moimId);
        moimMemberRepository.deleteAll(moim.getMembers());
        moimRepository.delete(moim);
    }
}
