package unicon.Achiva.domain.cheering.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.cheering.entity.Cheering;
import unicon.Achiva.domain.member.entity.Member;

import java.util.List;
import java.util.UUID;

public interface CheeringRepository extends JpaRepository<Cheering, Long>, CheeringRepositoryCustom {

    List<Cheering> findAllByIdInAndReceiver_Id(List<Long> ids, UUID receiverId);

    Page<Cheering> findAllByArticleId(UUID articleId, Pageable pageable);

    @Query("""
                select distinct c.sender.id
                from Cheering c
                where c.article.member.id = :me
            """)
    List<UUID> findDistinctCheererIdsWhoCheeredMyArticles(@Param("me") UUID me);

    Long countByArticle_MemberAndIsReadFalse(Member member);

    Page<Cheering> findAllByArticle_Member_Id(UUID memberId, Pageable pageable);


    /**
     * 내가 보낸 응원의 카테고리별 개수/점수(=count*pt)를 반환합니다.
     *
     * @param memberId       대상 사용자 ID
     * @param pointsPerCheer 응원 1건당 점수
     * @return 카테고리별 통계 프로젝션 목록
     */
    @Query("""
                select
                    c.cheeringCategory as cheeringCategory,
                    count(c) as count,
                    count(c) * :pt as points
                from Cheering c
                where c.sender.id = :memberId
                group by c.cheeringCategory
            """)
    List<CategoryStatProjection> givenStatsByCategory(@Param("memberId") UUID memberId,
                                                      @Param("pt") long pointsPerCheer);

    /**
     * 내가 받은 응원의 카테고리별 개수/점수(=count*pt)를 반환합니다.
     *
     * @param memberId       대상 사용자 ID
     * @param pointsPerCheer 응원 1건당 점수
     * @return 카테고리별 통계 프로젝션 목록
     */
    @Query("""
            select
                c.cheeringCategory     as cheeringCategory,
                count(c)       as count,
                count(c) * :pt as points
            from Cheering c
            where c.receiver.id = :memberId
            group by c.cheeringCategory
            """)
    List<CategoryStatProjection> receivedStatsByCategory(@Param("memberId") UUID memberId,
                                                         @Param("pt") long pointsPerCheer);

    // 멤버의 총 준 응원 개수
    @Query("select count(c) from Cheering c where c.sender.id = :memberId")
    long totalGivenCount(@Param("memberId") UUID memberId);

    // 멤버의 총 받은 응원 개수
    @Query("select count(c) from Cheering c where c.receiver.id = :memberId")
    long totalReceivedCount(@Param("memberId") UUID memberId);

    /**
     * 특정 사용자와 응원 관계가 있는 모든 사용자 ID를 조회합니다.
     * (내가 응원을 보낸 사람 + 나에게 응원을 보낸 사람) - 나 자신
     *
     * @param memberId 현재 사용자 ID
     * @return 응원 관계가 있는 사용자 ID 목록 (중복 제거)
     */
    @Query("""
            select distinct case
                when c.sender.id = :memberId then c.receiver.id
                else c.sender.id
            end
            from Cheering c
            where (c.sender.id = :memberId or c.receiver.id = :memberId)
              and case
                  when c.sender.id = :memberId then c.receiver.id
                  else c.sender.id
              end <> :memberId
            """)
    List<UUID> findCheeringRelatedMemberIds(@Param("memberId") UUID memberId);
}
