package unicon.Achiva.domain.friendship.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.friendship.FriendshipStatus;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.global.common.LongBaseEntity;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends LongBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member receiver;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    @Builder.Default
    private boolean receiverAllowsPostPush = true;

    @Builder.Default
    private boolean requesterAllowsPostPush = true;

    public void updateReceiver(Member receiver) {
        this.receiver = receiver;
    }

    public void updateReceiverAllowsPostPush(boolean allowsPostPush) {
        this.receiverAllowsPostPush = allowsPostPush;
    }

    public void updateRequesterAllowsPostPush(boolean allowsPostPush) {
        this.requesterAllowsPostPush = allowsPostPush;
    }

    public void updateStatus(FriendshipStatus acceptStatus) {
        this.status = acceptStatus;
    }
}
