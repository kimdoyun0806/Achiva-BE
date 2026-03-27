package unicon.Achiva.domain.moim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.moim.entity.MoimScore;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoimScoreRepository extends JpaRepository<MoimScore, Long> {

    @Query("""
            SELECT ms
              FROM MoimScore ms
              JOIN FETCH ms.moim
             WHERE ms.member.id = :memberId
               AND ms.leftAt IS NULL
            """)
    List<MoimScore> findActiveScoresByMemberId(@Param("memberId") UUID memberId);

    @Query("""
            SELECT ms
              FROM MoimScore ms
              JOIN FETCH ms.moim
             WHERE ms.member.id = :memberId
               AND ms.createdAt <= :articleCreatedAt
               AND (ms.leftAt IS NULL OR ms.leftAt >= :articleCreatedAt)
            """)
    List<MoimScore> findScoresContainingArticleCreatedAt(
            @Param("memberId") UUID memberId,
            @Param("articleCreatedAt") LocalDateTime articleCreatedAt
    );

    List<MoimScore> findByMoim_IdAndMember_IdInAndLeftAtIsNull(Long moimId, Collection<UUID> memberIds);

    Optional<MoimScore> findByMoim_IdAndMember_IdAndLeftAtIsNull(Long moimId, UUID memberId);

    void deleteAllByMoim_Id(Long moimId);
}
