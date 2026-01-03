package unicon.Achiva.domain.friendship.dto;

import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.friendship.FriendshipStatus;
import unicon.Achiva.domain.friendship.entity.Friendship;

import java.util.UUID;

@Getter
@Builder
public class FriendshipResponse {
    private Long id;
    private UUID requesterId;
    private UUID receiverId;
    private FriendshipStatus status;

    public static FriendshipResponse fromEntity(Friendship friendship) {
        return FriendshipResponse.builder()
                .id(friendship.getId())
                .requesterId(friendship.getRequester().getId())
                .receiverId(friendship.getReceiver().getId())
                .status(friendship.getStatus())
                .build();
    }
}
