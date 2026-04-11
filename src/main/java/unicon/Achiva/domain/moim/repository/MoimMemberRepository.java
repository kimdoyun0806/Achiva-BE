package unicon.Achiva.domain.moim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.moim.entity.MoimMember;
import java.util.List;
import java.util.UUID;

public interface MoimMemberRepository extends JpaRepository<MoimMember, Long> {
    List<MoimMember> findByMoimId(Long moimId);
    List<MoimMember> findByMemberId(UUID memberId);

    @Query("""
            select distinct joinedMember.member.id
            from MoimMember myMoim
            join MoimMember joinedMember on joinedMember.moim.id = myMoim.moim.id
            where myMoim.member.id = :memberId
            """)
    List<UUID> findDistinctJoinedMemberIdsByMemberId(@Param("memberId") UUID memberId);

    boolean existsByMoimIdAndMemberId(Long moimId, UUID memberId);
    java.util.Optional<MoimMember> findByMoimIdAndMemberId(Long moimId, UUID memberId);
}
