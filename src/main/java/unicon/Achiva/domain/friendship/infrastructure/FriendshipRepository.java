package unicon.Achiva.domain.friendship.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
