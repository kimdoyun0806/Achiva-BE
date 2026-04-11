package unicon.Achiva.domain.scripture.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import unicon.Achiva.domain.scripture.entity.MemberScriptureProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberScriptureProgressRepository extends JpaRepository<MemberScriptureProgress, UUID> {
    List<MemberScriptureProgress> findAllByMemberIdAndIsDeletedFalseOrderByUpdatedAtDesc(UUID memberId);

    Optional<MemberScriptureProgress> findByMemberIdAndScriptureIdAndIsDeletedFalse(UUID memberId, String scriptureId);
}
