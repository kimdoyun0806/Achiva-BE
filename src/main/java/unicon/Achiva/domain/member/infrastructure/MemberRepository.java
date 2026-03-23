package unicon.Achiva.domain.member.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Member> findByNickNameContainingIgnoreCase(String nickName, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select m
            from Member m
            where m.id in :memberIds
            order by m.id asc
            """)
    List<Member> findAllByIdInOrderByIdAscForUpdate(@Param("memberIds") Collection<UUID> memberIds);
}
