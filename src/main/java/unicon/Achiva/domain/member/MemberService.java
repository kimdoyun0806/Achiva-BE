package unicon.Achiva.domain.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.article.ArticleService;
import unicon.Achiva.domain.article.infrastructure.ArticleRepository;
import unicon.Achiva.domain.member.dto.ConfirmProfileImageUploadRequest;
import unicon.Achiva.domain.member.dto.MemberRankingResponse;
import unicon.Achiva.domain.member.dto.MemberResponse;
import unicon.Achiva.domain.member.dto.MemberStatsResponse;
import unicon.Achiva.domain.member.dto.SearchMemberCondition;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.global.response.GeneralException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    public Boolean existsById(UUID memberId) {
        return memberRepository.existsById(memberId);
    }

    public MemberResponse getMemberInfo(UUID memberId) {
        return memberRepository.findById(memberId)
                .map(member -> MemberResponse.fromEntity(member, getArticleCount(member.getId())))
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public MemberResponse getMemberInfoByNickname(String nickname) {
        return memberRepository.findByNickName(nickname)
                .map(member -> MemberResponse.fromEntity(member, getArticleCount(member.getId())))
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public Page<MemberResponse> getMembers(SearchMemberCondition condition, Pageable pageable) {
        Page<Member> members = memberRepository.findByNickNameContainingIgnoreCase(condition.getKeyword(), pageable);
        Map<UUID, Long> articleCountMap = getArticleCountMap(
                members.getContent().stream()
                        .map(Member::getId)
                        .toList()
        );

        List<MemberResponse> content = members.getContent().stream()
                .map(member -> MemberResponse.fromEntity(member, articleCountMap.getOrDefault(member.getId(), 0L)))
                .toList();

        return new PageImpl<>(content, pageable, members.getTotalElements());
    }

    public List<MemberRankingResponse> getMembersForRanking() {
        List<Member> members = memberRepository.findAll(Sort.by(Sort.Direction.ASC, "nickName"));
        List<UUID> memberIds = members.stream()
                .map(Member::getId)
                .toList();

        Map<UUID, Long> articleCountMap = getArticleCountMap(memberIds);
        Map<UUID, MemberStatsResponse> memberStatsMap = articleService.getMemberStatsMap(memberIds);

        return members.stream()
                .map(member -> MemberRankingResponse.from(
                        member,
                        articleCountMap.getOrDefault(member.getId(), 0L),
                        memberStatsMap.getOrDefault(member.getId(), new MemberStatsResponse(0, 0))
                ))
                .toList();
    }

    /**
     * 주어진 회원의 프로필 이미지 URL을 갱신한다.
     *
     * @param memberId 회원 식별자
     * @param request  업로드 확인 요청(이미지 URL 포함)
     * @throws GeneralException 회원이 존재하지 않는 경우
     */
    @Transactional
    public void updateProfileImageUrl(UUID memberId, ConfirmProfileImageUploadRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updateProfileImageUrl(request.getUrl());
    }

    /**
     * 회원의 푸시 알림 사용 여부를 변경한다.
     *
     * @param memberId   회원 식별자
     * @param enabled    true: 푸시 알림 허용, false: 푸시 알림 비활성화
     */
    @Transactional
    public void updatePushEnabled(UUID memberId, boolean enabled) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updatePushEnabled(enabled);
    }

    /**
     * 회원의 친구 운동 게시글 푸시 알림 사용 여부를 변경한다.
     *
     * @param memberId   회원 식별자
     * @param enabled    true: 친구 운동 푸시 알림 허용, false: 비활성화
     */
    @Transactional
    public void updateFriendWorkoutPushEnabled(UUID memberId, boolean enabled) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updateFriendWorkoutPushEnabled(enabled);
        log.info("[Member] 친구 운동 푸시 알림 설정 변경 - memberId: {}, enabled: {}", memberId, enabled);
    }

    private long getArticleCount(UUID memberId) {
        return articleRepository.countArticlesByDateRange(memberId, null, null);
    }

    private Map<UUID, Long> getArticleCountMap(List<UUID> memberIds) {
        if (memberIds.isEmpty()) {
            return Map.of();
        }

        return articleRepository.countArticlesByMemberIds(memberIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1],
                        (existing, ignored) -> existing,
                        java.util.HashMap::new
                ));
    }
}
