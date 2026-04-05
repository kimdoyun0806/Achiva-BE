package unicon.Achiva.domain.friendship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.friendship.dto.FriendshipRequest;
import unicon.Achiva.domain.friendship.dto.FriendshipResponse;
import unicon.Achiva.domain.friendship.entity.Friendship;
import unicon.Achiva.domain.friendship.infrastructure.FriendshipRepository;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.domain.push.PushService;
import unicon.Achiva.domain.push.dto.PushSendRequest;
import unicon.Achiva.global.response.GeneralException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipService {

    private static final Comparator<Friendship> FRIENDSHIP_PRIORITY = Comparator
            .comparingInt((Friendship friendship) -> statusPriority(friendship.getStatus())).reversed()
            .thenComparing(Friendship::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Friendship::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Friendship::getId, Comparator.nullsLast(Comparator.reverseOrder()));

    private final FriendshipRepository friendshipRepository;
    private final MemberRepository memberRepository;
    private final PushService pushService;

    @Transactional
    public FriendshipResponse sendFriendRequest(FriendshipRequest friendshipRequest, UUID fromMemberId) {
        UUID receiverId = friendshipRequest.getRecieverId();
        if (fromMemberId.equals(receiverId)) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_SELF_REQUEST);
        }

        Map<UUID, Member> members = lockMembers(fromMemberId, receiverId);
        Member requester = members.get(fromMemberId);
        Member receiver = members.get(receiverId);
        List<Friendship> pairFriendships = friendshipRepository.findAllByMemberPairForUpdate(fromMemberId, receiverId);

        Friendship blockedFriendship = findLatestByStatus(pairFriendships, FriendshipStatus.BLOCKED);
        if (blockedFriendship != null) {
            cleanupPairDuplicates(pairFriendships, blockedFriendship);
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_BLOCKED);
        }

        Friendship acceptedFriendship = findLatestByStatus(pairFriendships, FriendshipStatus.ACCEPTED);
        if (acceptedFriendship != null) {
            cleanupPairDuplicates(pairFriendships, acceptedFriendship);
            return FriendshipResponse.fromEntity(acceptedFriendship);
        }

        Friendship reversePendingFriendship = pairFriendships.stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .filter(friendship -> friendship.getRequester().getId().equals(receiverId))
                .filter(friendship -> friendship.getReceiver().getId().equals(fromMemberId))
                .max(FRIENDSHIP_PRIORITY)
                .orElse(null);
        if (reversePendingFriendship != null) {
            reversePendingFriendship.updateStatus(FriendshipStatus.ACCEPTED);
            cleanupPairDuplicates(pairFriendships, reversePendingFriendship);
            sendFriendAcceptPushNotification(requester, receiver);
            return FriendshipResponse.fromEntity(reversePendingFriendship);
        }

        Friendship sameDirectionPendingFriendship = pairFriendships.stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING)
                .filter(friendship -> friendship.getRequester().getId().equals(fromMemberId))
                .filter(friendship -> friendship.getReceiver().getId().equals(receiverId))
                .max(FRIENDSHIP_PRIORITY)
                .orElse(null);
        if (sameDirectionPendingFriendship != null) {
            cleanupPairDuplicates(pairFriendships, sameDirectionPendingFriendship);
            return FriendshipResponse.fromEntity(sameDirectionPendingFriendship);
        }

        Friendship reusableRejectedFriendship = findLatestByStatus(pairFriendships, FriendshipStatus.REJECTED);
        if (reusableRejectedFriendship != null) {
            reusableRejectedFriendship.updateParticipants(requester, receiver);
            reusableRejectedFriendship.updateStatus(FriendshipStatus.PENDING);
            cleanupPairDuplicates(pairFriendships, reusableRejectedFriendship);
            sendFriendRequestPushNotification(requester, receiver);
            return FriendshipResponse.fromEntity(reusableRejectedFriendship);
        }

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);

        // 푸시 알림 전송: 친구 요청 (friend_request)
        sendFriendRequestPushNotification(requester, receiver);

        return FriendshipResponse.fromEntity(friendship);
    }

    @Transactional
    public FriendshipResponse acceptFriendRequest(Long friendshipId, UUID memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
            return FriendshipResponse.fromEntity(friendship);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        if (!friendship.getReceiver().getId().equals(memberId)) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_RECEIVER);
        }

        lockMembers(friendship.getRequester().getId(), friendship.getReceiver().getId());
        List<Friendship> pairFriendships = friendshipRepository.findAllByMemberPairForUpdate(
                friendship.getRequester().getId(),
                friendship.getReceiver().getId()
        );
        friendship.updateStatus(FriendshipStatus.ACCEPTED);
        cleanupPairDuplicates(pairFriendships, friendship);

        // 푸시 알림 전송: 친구 수락 (friend_accept)
        sendFriendAcceptPushNotification(friendship.getReceiver(), friendship.getRequester());

        return FriendshipResponse.fromEntity(friendship);
    }

    @Transactional
    public FriendshipResponse rejectFriendRequest(Long friendshipId, UUID memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() == FriendshipStatus.REJECTED) {
            return FriendshipResponse.fromEntity(friendship);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        if (!friendship.getReceiver().getId().equals(memberId)) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_RECEIVER);
        }

        lockMembers(friendship.getRequester().getId(), friendship.getReceiver().getId());
        List<Friendship> pairFriendships = friendshipRepository.findAllByMemberPairForUpdate(
                friendship.getRequester().getId(),
                friendship.getReceiver().getId()
        );
        friendship.updateStatus(FriendshipStatus.REJECTED);
        cleanupPairDuplicates(pairFriendships, friendship);
        friendshipRepository.save(friendship);
        return FriendshipResponse.fromEntity(friendship);
    }

    public List<FriendshipResponse> getFriendRequests(UUID memberId) {
        return friendshipRepository.findByReceiverIdAndStatus(memberId, FriendshipStatus.PENDING)
                .stream()
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FriendshipResponse> getSentFriendRequests(UUID memberId) {
        return friendshipRepository.findByRequesterIdAndStatus(memberId, FriendshipStatus.PENDING)
                .stream()
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FriendshipResponse> getFriends(UUID memberId) {
        return friendshipRepository.findByRequesterIdOrReceiverId(memberId, memberId)
                .stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FriendshipResponse> getFriendsByNickname(String nickname) {
        UUID memberId = memberRepository.findByNickName(nickname)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND))
                .getId();

        return friendshipRepository.findByRequesterIdOrReceiverId(memberId, memberId)
                .stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                .map(FriendshipResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void blockFriendship(Long friendshipId, UUID memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FRIENDS);
        }

        if (friendship.getReceiver().getId().equals(memberId)) {
            friendship.updateStatus(FriendshipStatus.BLOCKED);
        } else {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_RECEIVER);
        }
    }

    @Transactional
    public void cancelFriendRequest(Long friendshipId, UUID memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        if (friendship.getRequester().getId().equals(memberId)) {
            friendshipRepository.delete(friendship);
        } else {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_REQUESTER);
        }
    }

    @Transactional
    public void updatePostPushSetting(Long friendshipId, UUID memberId, boolean allowsPostPush) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FRIENDS);
        }

        // requester 또는 receiver 중 누가 설정을 변경하는지 확인
        if (friendship.getReceiver().getId().equals(memberId)) {
            friendship.updateReceiverAllowsPostPush(allowsPostPush);
            log.info("[Friendship] Receiver 게시글 푸시 설정 변경 - friendshipId: {}, receiverId: {}, allowsPostPush: {}",
                    friendshipId, memberId, allowsPostPush);
        } else if (friendship.getRequester().getId().equals(memberId)) {
            friendship.updateRequesterAllowsPostPush(allowsPostPush);
            log.info("[Friendship] Requester 게시글 푸시 설정 변경 - friendshipId: {}, requesterId: {}, allowsPostPush: {}",
                    friendshipId, memberId, allowsPostPush);
        } else {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND);
        }
    }

    /**
     * 친구 요청 푸시 알림 전송
     * 에러 발생 시에도 비즈니스 로직에 영향을 주지 않도록 예외 처리
     */
    private void sendFriendRequestPushNotification(Member requester, Member receiver) {
        try {
            // 수신자의 pushEnabled 확인은 PushService에서 처리됨
            String title = "새로운 친구 요청";
            String body = String.format("%s님이 회원님에게 친구 요청을 남겼어요.", requester.getNickName());

            Map<String, Object> data = new HashMap<>();
            data.put("type", "friend_request");
            data.put("fromUserId", requester.getId().toString());

            PushSendRequest pushRequest = PushSendRequest.builder()
                    .targetMemberId(receiver.getId())
                    .title(title)
                    .body(body)
                    .data(data)
                    .build();

            pushService.sendPushNotification(requester.getId(), pushRequest);
            log.info("[Friendship] 친구 요청 푸시 알림 전송 성공 - from: {}, to: {}",
                     requester.getId(), receiver.getId());
        } catch (Exception e) {
            // 푸시 전송 실패 시 로그만 남기고 비즈니스 로직은 계속 진행
            log.error("[Friendship] 친구 요청 푸시 알림 전송 실패 - from: {}, to: {}, error: {}",
                      requester.getId(), receiver.getId(), e.getMessage(), e);
        }
    }

    /**
     * 친구 수락 푸시 알림 전송
     * 에러 발생 시에도 비즈니스 로직에 영향을 주지 않도록 예외 처리
     */
    private void sendFriendAcceptPushNotification(Member acceptor, Member requester) {
        try {
            // 수신자의 pushEnabled 확인은 PushService에서 처리됨
            String title = String.format("%s님과 친구가 되었어요", acceptor.getNickName());
            String body = "새로운 친구에게 응원코멘트를 남겨보세요.";

            Map<String, Object> data = new HashMap<>();
            data.put("type", "friend_accept");
            data.put("fromUserId", acceptor.getId().toString());

            PushSendRequest pushRequest = PushSendRequest.builder()
                    .targetMemberId(requester.getId())
                    .title(title)
                    .body(body)
                    .data(data)
                    .build();

            pushService.sendPushNotification(acceptor.getId(), pushRequest);
            log.info("[Friendship] 친구 수락 푸시 알림 전송 성공 - from: {}, to: {}",
                     acceptor.getId(), requester.getId());
        } catch (Exception e) {
            // 푸시 전송 실패 시 로그만 남기고 비즈니스 로직은 계속 진행
            log.error("[Friendship] 친구 수락 푸시 알림 전송 실패 - from: {}, to: {}, error: {}",
                      acceptor.getId(), requester.getId(), e.getMessage(), e);
        }
    }

    private Map<UUID, Member> lockMembers(UUID firstMemberId, UUID secondMemberId) {
        List<UUID> memberIds = List.of(firstMemberId, secondMemberId).stream()
                .sorted()
                .toList();

        List<Member> members = memberRepository.findAllByIdInOrderByIdAscForUpdate(memberIds);
        if (members.size() != 2) {
            throw new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        return members.stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
    }

    private Friendship findLatestByStatus(List<Friendship> friendships, FriendshipStatus status) {
        return friendships.stream()
                .filter(friendship -> friendship.getStatus() == status)
                .max(FRIENDSHIP_PRIORITY)
                .orElse(null);
    }

    private void cleanupPairDuplicates(List<Friendship> friendships, Friendship canonicalFriendship) {
        List<Friendship> duplicates = friendships.stream()
                .filter(friendship -> !friendship.getId().equals(canonicalFriendship.getId()))
                .toList();

        if (!duplicates.isEmpty()) {
            friendshipRepository.deleteAll(duplicates);
        }
    }

    private static int statusPriority(FriendshipStatus status) {
        return switch (status) {
            case BLOCKED -> 4;
            case ACCEPTED -> 3;
            case PENDING -> 2;
            case REJECTED -> 1;
        };
    }
}
