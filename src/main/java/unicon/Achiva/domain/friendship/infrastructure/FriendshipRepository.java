package unicon.Achiva.domain.friendship.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.friendship.FriendshipStatus;
import unicon.Achiva.domain.friendship.entity.Friendship;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByReceiverIdAndStatus(UUID memberId, FriendshipStatus friendshipStatus);

    List<Friendship> findByRequesterIdOrReceiverId(UUID memberId, UUID memberId1);

    List<Friendship> findByRequesterIdAndStatus(UUID memberId, FriendshipStatus friendshipStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select f
            from Friendship f
            where (f.requester.id = :firstMemberId and f.receiver.id = :secondMemberId)
               or (f.requester.id = :secondMemberId and f.receiver.id = :firstMemberId)
            """)
    List<Friendship> findAllByMemberPairForUpdate(@Param("firstMemberId") UUID firstMemberId,
                                                  @Param("secondMemberId") UUID secondMemberId);

    @Query("""
            select case
                when f.requester.id = :me then f.receiver.id
                else f.requester.id
            end
            from Friendship f
            where (f.requester.id = :me or f.receiver.id = :me)
              and f.status = :status
            """)
    List<UUID> findFriendIdsOf(@Param("me") UUID me,
                               @Param("status") FriendshipStatus status);

    @Query("""
            select f
            from Friendship f
            where f.requester.id = :memberId
              and f.status = :status
            """)
    List<Friendship> findAllByRequesterIdAndStatus(@Param("memberId") UUID memberId,
                                                    @Param("status") FriendshipStatus status);

    @Query("""
            select f
            from Friendship f
            where (f.requester.id = :memberId or f.receiver.id = :memberId)
              and f.status = :status
            """)
    List<Friendship> findAllAcceptedFriendships(@Param("memberId") UUID memberId,
                                                 @Param("status") FriendshipStatus status);
}
