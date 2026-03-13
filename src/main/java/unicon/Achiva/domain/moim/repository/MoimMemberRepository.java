package unicon.Achiva.domain.moim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicon.Achiva.domain.moim.entity.MoimMember;
import java.util.List;
import java.util.UUID;

public interface MoimMemberRepository extends JpaRepository<MoimMember, Long> {
    List<MoimMember> findByMoimId(Long moimId);
    List<MoimMember> findByMemberId(UUID memberId);
    boolean existsByMoimIdAndMemberId(Long moimId, UUID memberId);
}
