package unicon.Achiva.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unicon.Achiva.domain.article.ArticleService;
import unicon.Achiva.domain.article.dto.TotalCharacterCountResponse;
import unicon.Achiva.domain.article.infrastructure.ArticleRepository;
import unicon.Achiva.domain.auth.Role;
import unicon.Achiva.domain.cheering.CheeringService;
import unicon.Achiva.domain.cheering.dto.TotalReceivedCheeringScoreResponse;
import unicon.Achiva.domain.cheering.dto.TotalSendingCheeringScoreResponse;
import unicon.Achiva.domain.member.dto.MemberDetailResponse;
import unicon.Achiva.domain.member.dto.MemberStatsResponse;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.domain.organization.OrganizationAccessService;
import unicon.Achiva.domain.organization.entity.Organization;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleService articleService;

    @Mock
    private CheeringService cheeringService;

    @Mock
    private OrganizationAccessService organizationAccessService;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(
                memberRepository,
                articleRepository,
                articleService,
                cheeringService,
                organizationAccessService
        );
    }

    @Test
    void getMemberDetailInfoAggregatesExistingMetrics() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID targetMemberId = UUID.randomUUID();
        Member member = member(targetMemberId, "target@test.com", "target");

        when(organizationAccessService.getAccessibleMember(requesterId, targetMemberId)).thenReturn(member);
        when(articleRepository.countArticlesByDateRange(targetMemberId, null, null)).thenReturn(12L);
        when(articleService.getMemberStats(targetMemberId)).thenReturn(new MemberStatsResponse(4, 3));
        when(articleService.getTotalCharacterCountByDateRange(
                targetMemberId,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                null
        )).thenReturn(new TotalCharacterCountResponse(15420L));
        when(cheeringService.getTotalGivenPoints(targetMemberId)).thenReturn(new TotalSendingCheeringScoreResponse(120L));
        when(cheeringService.getTotalReceivedPoints(targetMemberId)).thenReturn(new TotalReceivedCheeringScoreResponse(230L));

        MemberDetailResponse response = memberService.getMemberDetailInfo(requesterId, targetMemberId);

        assertThat(response.getId()).isEqualTo(targetMemberId);
        assertThat(response.getNickName()).isEqualTo("target");
        assertThat(response.getArticleCount()).isEqualTo(12L);
        assertThat(response.getWeeklyWorkoutCount()).isEqualTo(4);
        assertThat(response.getContinuousGoalWeeks()).isEqualTo(3);
        assertThat(response.getTotalCharacterCountFrom2025()).isEqualTo(15420L);
        assertThat(response.getTotalSendingCheeringScore()).isEqualTo(120L);
        assertThat(response.getTotalReceivingCheeringScore()).isEqualTo(230L);
    }

    @Test
    void getMemberDetailInfoByNicknameUsesOrganizationScopedNicknameLookup() throws Exception {
        UUID requesterId = UUID.randomUUID();
        Long organizationId = 7L;
        Member member = member(UUID.randomUUID(), "target@test.com", "target");

        when(organizationAccessService.getOrganizationId(requesterId)).thenReturn(organizationId);
        when(memberRepository.findByNickNameAndOrganization_Id("target", organizationId)).thenReturn(Optional.of(member));
        when(articleRepository.countArticlesByDateRange(member.getId(), null, null)).thenReturn(5L);
        when(articleService.getMemberStats(member.getId())).thenReturn(new MemberStatsResponse(1, 2));
        when(articleService.getTotalCharacterCountByDateRange(
                member.getId(),
                LocalDateTime.of(2025, 1, 1, 0, 0),
                null
        )).thenReturn(new TotalCharacterCountResponse(300L));
        when(cheeringService.getTotalGivenPoints(member.getId())).thenReturn(new TotalSendingCheeringScoreResponse(40L));
        when(cheeringService.getTotalReceivedPoints(member.getId())).thenReturn(new TotalReceivedCheeringScoreResponse(70L));

        MemberDetailResponse response = memberService.getMemberDetailInfoByNickname(requesterId, "target");

        assertThat(response.getNickName()).isEqualTo("target");
        assertThat(response.getOrganizationId()).isEqualTo(organizationId);
        assertThat(response.getTotalReceivingCheeringScore()).isEqualTo(70L);
        verify(memberRepository).findByNickNameAndOrganization_Id("target", organizationId);
    }

    private static Member member(UUID id, String email, String nickname) throws Exception {
        Organization organization = Organization.builder()
                .name("Achiva University")
                .build();
        setOrganizationId(organization, 7L);

        return Member.builder()
                .id(id)
                .email(email)
                .nickName(nickname)
                .role(Role.USER)
                .organization(organization)
                .build();
    }

    private static void setOrganizationId(Organization organization, Long id) throws Exception {
        Field idField = organization.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(organization, id);
    }
}
