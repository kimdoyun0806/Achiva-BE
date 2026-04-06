package unicon.Achiva.domain.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.article.ArticleErrorCode;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.article.infrastructure.ArticleRepository;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.domain.moim.MoimErrorCode;
import unicon.Achiva.domain.moim.entity.Moim;
import unicon.Achiva.domain.moim.repository.MoimRepository;
import unicon.Achiva.global.response.GeneralException;

import java.util.UUID;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationAccessService {

    private final MemberRepository memberRepository;
    private final MoimRepository moimRepository;
    private final ArticleRepository articleRepository;

    public Member getMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public Long getOrganizationId(UUID memberId) {
        return getMember(memberId).getOrganization().getId();
    }

    public Member getAccessibleMember(UUID requesterId, UUID targetMemberId) {
        Member requester = getMember(requesterId);
        Member target = getMember(targetMemberId);

        if (!requester.getOrganization().getId().equals(target.getOrganization().getId())) {
            throw new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        return target;
    }

    public void validateSameOrganization(UUID requesterId, UUID targetMemberId) {
        getAccessibleMember(requesterId, targetMemberId);
    }

    public boolean isSameOrganization(UUID requesterId, UUID targetMemberId) {
        try {
            return getOrganizationId(requesterId).equals(getOrganizationId(targetMemberId));
        } catch (GeneralException e) {
            return false;
        }
    }

    public List<UUID> filterMemberIdsByOrganization(UUID requesterId, Collection<UUID> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }
        Long organizationId = getOrganizationId(requesterId);
        return memberRepository.findIdsByOrganizationIdAndIdIn(organizationId, memberIds);
    }

    public Moim getAccessibleMoim(UUID requesterId, Long moimId) {
        Long organizationId = getOrganizationId(requesterId);
        return moimRepository.findByIdAndOrganization_Id(moimId, organizationId)
                .orElseThrow(() -> new GeneralException(MoimErrorCode.MOIM_NOT_FOUND));
    }

    public Article getAccessibleArticle(UUID requesterId, UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        Long requesterOrganizationId = getOrganizationId(requesterId);
        Long authorOrganizationId = article.getMember().getOrganization().getId();
        if (!requesterOrganizationId.equals(authorOrganizationId)) {
            throw new GeneralException(ArticleErrorCode.ARTICLE_NOT_FOUND);
        }

        return article;
    }
}
