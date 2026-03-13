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
import unicon.Achiva.domain.moim.dto.MoimSettingRequest;
import unicon.Achiva.domain.moim.entity.Moim;
import unicon.Achiva.domain.moim.entity.MoimMember;
import unicon.Achiva.domain.moim.entity.MoimRole;
import unicon.Achiva.domain.moim.repository.MoimMemberRepository;
import unicon.Achiva.domain.moim.repository.MoimRepository;
import unicon.Achiva.global.response.GeneralException;

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

    @Transactional
    public MoimResponse createMoim(MoimCreateRequest request, UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        String encodedPassword = null;
        if (request.isPrivate() && request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = request.getPassword(); // 평문 저장 (임시)
        }

        Moim moim = Moim.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxMember(request.getMaxMember())
                .isPrivate(request.isPrivate())
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

        return MoimResponse.from(savedMoim);
    }

    public Page<MoimResponse> getMoims(String keyword, List<Category> categories, Pageable pageable) {
        boolean hasCategories = categories != null && !categories.isEmpty();
        Page<Moim> moims = moimRepository.findMoimsBySearchAndCategory(keyword, categories, hasCategories, pageable);
        return moims.map(MoimResponse::from);
    }

    public MoimDetailResponse getMoimDetail(Long moimId, UUID currentMemberId) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        // 이번 달 시작일
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 모임 멤버 UUID 목록
        List<UUID> memberIds = moim.getMembers().stream()
                .map(mm -> mm.getMember().getId())
                .collect(Collectors.toList());

        // 멤버별 이번 달 게시물 수
        Map<UUID, Long> postCountMap = new HashMap<>();
        if (!memberIds.isEmpty()) {
            List<Object[]> counts = articleRepository.countMonthlyPostsByMemberIds(memberIds, monthStart);
            for (Object[] row : counts) {
                UUID memberId = (UUID) row[0];
                Long count = (Long) row[1];
                postCountMap.put(memberId, count);
            }
        }

        return MoimDetailResponse.from(moim, currentMemberId, postCountMap);
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
    }

    @Transactional
    public MoimDetailResponse updateMoimSettings(Long moimId, UUID memberId, MoimSettingRequest request) {
        Moim moim = moimRepository.findById(moimId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));

        boolean isLeader = moim.getMembers().stream()
                .anyMatch(mm -> mm.getMember().getId().equals(memberId) && mm.getRole() == MoimRole.LEADER);

        if (!isLeader) {
            throw new GeneralException(MoimErrorCode.UNAUTHORIZED_ACTION);
        }

        moim.updateSettings(request.getTargetAmount(), request.getPokeDays());
        return MoimDetailResponse.from(moim, memberId, new HashMap<>());
    }
}
