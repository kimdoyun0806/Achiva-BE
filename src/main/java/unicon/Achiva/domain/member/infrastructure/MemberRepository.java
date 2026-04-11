package unicon.Achiva.domain.member.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.member.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    boolean existsByEmail(String email);

    boolean existsByNickName(String nickname);

    Optional<Member> findByNickName(String nickname);

    Optional<Member> findByNickNameAndOrganization_Id(String nickName, Long organizationId);

    Page<Member> findByNickNameContainingIgnoreCase(String nickName, Pageable pageable);

    Page<Member> findByOrganization_IdAndNickNameContainingIgnoreCase(Long organizationId, String nickName, Pageable pageable);

    List<Member> findAllByOrganization_Id(Long organizationId, Sort sort);

    @Query("""
            select m.id
            from Member m
            where m.organization.id = :organizationId
              and m.id in :memberIds
            """)
    List<UUID> findIdsByOrganizationIdAndIdIn(@Param("organizationId") Long organizationId,
                                              @Param("memberIds") Collection<UUID> memberIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select m
            from Member m
            where m.id in :memberIds
            order by m.id asc
            """)
    List<Member> findAllByIdInOrderByIdAscForUpdate(@Param("memberIds") Collection<UUID> memberIds);
}
