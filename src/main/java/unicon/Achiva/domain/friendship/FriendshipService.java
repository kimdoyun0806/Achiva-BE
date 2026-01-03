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
import unicon.Achiva.global.response.GeneralException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FriendshipResponse sendFriendRequest(FriendshipRequest friendshipRequest, UUID fromMemberId) {


        Member requester = memberRepository.findById(fromMemberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        Member receiver = memberRepository.findById(friendshipRequest.getRecieverId())
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);
        return FriendshipResponse.fromEntity(friendship);
    }

    @Transactional
    public FriendshipResponse acceptFriendRequest(Long friendshipId, UUID memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        if (!friendship.getReceiver().getId().equals(memberId)) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_RECEIVER);
        }

        friendship.updateStatus(FriendshipStatus.ACCEPTED);

        return FriendshipResponse.fromEntity(friendship);
    }

    @Transactional
    public FriendshipResponse rejectFriendRequest(Long friendshipId, UUID memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_ALREADY_PROCESSED);
        }

        if (!friendship.getReceiver().getId().equals(memberId)) {
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_NOT_RECEIVER);
        }

        friendship.updateStatus(FriendshipStatus.REJECTED);
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
}
