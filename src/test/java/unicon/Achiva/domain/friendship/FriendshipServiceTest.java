package unicon.Achiva.domain.friendship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unicon.Achiva.domain.friendship.dto.FriendshipRequest;
import unicon.Achiva.domain.friendship.dto.FriendshipResponse;
import unicon.Achiva.domain.friendship.entity.Friendship;
import unicon.Achiva.domain.friendship.infrastructure.FriendshipRepository;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.domain.organization.OrganizationAccessService;
import unicon.Achiva.domain.push.PushService;
import unicon.Achiva.global.response.GeneralException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PushService pushService;

    @Mock
    private OrganizationAccessService organizationAccessService;

    private FriendshipService friendshipService;

    @BeforeEach
    void setUp() {
        friendshipService = new FriendshipService(friendshipRepository, memberRepository, pushService, organizationAccessService);
    }

    @Test
    void sendFriendRequestAutoAcceptsReversePendingRequest() throws Exception {
        UUID memberAId = UUID.randomUUID();
        UUID memberBId = UUID.randomUUID();
        Member memberA = member(memberAId, "a@test.com", "memberA");
        Member memberB = member(memberBId, "b@test.com", "memberB");
        List<UUID> lockedMemberIds = Stream.of(memberAId, memberBId).sorted().toList();

        Friendship reversePending = friendship(memberB, memberA, FriendshipStatus.PENDING, 10L);
        Friendship duplicatePending = friendship(memberA, memberB, FriendshipStatus.PENDING, 11L);

        when(memberRepository.findAllByIdInOrderByIdAscForUpdate(lockedMemberIds))
                .thenReturn(List.of(memberA, memberB));
        when(friendshipRepository.findAllByMemberPairForUpdate(memberAId, memberBId))
                .thenReturn(List.of(reversePending, duplicatePending));

        FriendshipRequest request = friendshipRequest(memberBId);

        FriendshipResponse response = friendshipService.sendFriendRequest(request, memberAId);

        assertThat(response.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        assertThat(reversePending.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        verify(friendshipRepository).deleteAll(List.of(duplicatePending));
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void sendFriendRequestReturnsExistingAcceptedFriendship() throws Exception {
        UUID memberAId = UUID.randomUUID();
        UUID memberBId = UUID.randomUUID();
        Member memberA = member(memberAId, "a@test.com", "memberA");
        Member memberB = member(memberBId, "b@test.com", "memberB");
        List<UUID> lockedMemberIds = Stream.of(memberAId, memberBId).sorted().toList();

        Friendship accepted = friendship(memberA, memberB, FriendshipStatus.ACCEPTED, 20L);
        Friendship stalePending = friendship(memberB, memberA, FriendshipStatus.PENDING, 21L);

        when(memberRepository.findAllByIdInOrderByIdAscForUpdate(lockedMemberIds))
                .thenReturn(List.of(memberA, memberB));
        when(friendshipRepository.findAllByMemberPairForUpdate(memberAId, memberBId))
                .thenReturn(List.of(accepted, stalePending));

        FriendshipResponse response = friendshipService.sendFriendRequest(friendshipRequest(memberBId), memberAId);

        assertThat(response.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        verify(friendshipRepository).deleteAll(List.of(stalePending));
        verify(pushService, never()).sendPushNotification(any(), any());
    }

    @Test
    void sendFriendRequestRejectsSelfRequest() throws Exception {
        UUID memberId = UUID.randomUUID();

        FriendshipRequest request = friendshipRequest(memberId);

        assertThatThrownBy(() -> friendshipService.sendFriendRequest(request, memberId))
                .isInstanceOf(GeneralException.class)
                .hasMessage(FriendshipErrorCode.FRIENDSHIP_SELF_REQUEST.getMessage());
    }

    private static Member member(UUID id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickName(nickname)
                .build();
    }

    private static Friendship friendship(Member requester, Member receiver, FriendshipStatus status, Long id) throws Exception {
        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(status)
                .build();

        Field idField = friendship.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(friendship, id);
        return friendship;
    }

    private static FriendshipRequest friendshipRequest(UUID receiverId) throws Exception {
        FriendshipRequest request = new FriendshipRequest();
        Field receiverIdField = FriendshipRequest.class.getDeclaredField("recieverId");
        receiverIdField.setAccessible(true);
        receiverIdField.set(request, receiverId);
        return request;
    }
}
