package unicon.Achiva.domain.friendship;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.friendship.dto.FriendshipRequest;
import unicon.Achiva.domain.friendship.dto.FriendshipResponse;
import unicon.Achiva.global.response.ApiResponseForm;
import unicon.Achiva.global.response.GeneralException;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final AuthService authService;

    @Operation(summary = "친구 신청")
    @PostMapping("/api/friendships")
    public ResponseEntity<ApiResponseForm<FriendshipResponse>> sendFriendRequest(
            @RequestBody FriendshipRequest friendshipRequest
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        FriendshipResponse response = friendshipService.sendFriendRequest(friendshipRequest, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "친구 신청 성공"));
    }

    @Operation(summary = "친구 신청 수락")
    @PatchMapping("/api/friendships/{friendshipId}/accept")
    public ResponseEntity<ApiResponseForm<FriendshipResponse>> acceptFriendRequest(
            @RequestParam Long friendshipId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        FriendshipResponse response = friendshipService.acceptFriendRequest(friendshipId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "친구 신청 수락 성공"));
    }

    @Operation(summary = "친구 신청 거절")
    @PatchMapping("/api/friendships/{friendshipId}/reject")
    public ResponseEntity<ApiResponseForm<FriendshipResponse>> rejectFriendRequest(
            @RequestParam Long friendshipId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        FriendshipResponse response = friendshipService.rejectFriendRequest(friendshipId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "친구 신청 거절 성공"));
    }

    @Operation(summary = "친구 목록 조회")
    @GetMapping("/api/friendships")
    public ResponseEntity<ApiResponseForm<List<FriendshipResponse>>> getFriendList(
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        List<FriendshipResponse> friendList = friendshipService.getFriends(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(friendList, "친구 목록 조회 성공"));
    }

    @Operation(summary = "특정 유저 친구 목록 조회")
    @GetMapping("/api/friendships/{memberId}")
    public ResponseEntity<ApiResponseForm<List<FriendshipResponse>>> getFriendListByMemberId(
            @PathVariable UUID memberId
    ) {
        List<FriendshipResponse> friendList = friendshipService.getFriends(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(friendList, "특정 유저 친구 목록 조회 성공"));
    }

    @Operation(summary = "특정 유저 친구 목록 조회 (닉네임으로)")
    @GetMapping("/api2/friendships/{nickname}")
    public ResponseEntity<ApiResponseForm<List<FriendshipResponse>>> getFriendListByNickname(
            @PathVariable String nickname
    ) {
        List<FriendshipResponse> friendList = friendshipService.getFriendsByNickname(nickname);
        return ResponseEntity.ok(ApiResponseForm.success(friendList, "닉네임으로 친구 목록 조회 성공"));
    }

    @Operation(summary = "내게 온 친구 신청 목록 조회")
    @GetMapping("/api/friendships/requests")
    public ResponseEntity<ApiResponseForm<List<FriendshipResponse>>> getFriendRequests(
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        List<FriendshipResponse> friendRequests = friendshipService.getFriendRequests(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(friendRequests, "친구 신청 목록 조회 성공"));
    }

    @Operation(summary = "내가 보낸 친구 신청 목록 조회")
    @GetMapping("/api/friendships/sent-requests")
    public ResponseEntity<ApiResponseForm<List<FriendshipResponse>>> getSentFriendRequests(
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        List<FriendshipResponse> sentRequests = friendshipService.getSentFriendRequests(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(sentRequests, "보낸 친구 신청 목록 조회 성공"));

    }

    @Operation(summary = "친구 차단")
    @PatchMapping("/api/friendships/{friendshipId}/block")
    public ResponseEntity<ApiResponseForm<Void>> deleteFriendRequest(
            @PathVariable Long friendshipId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        try { //분쟁 및 친구 목록 크롤링 방지를 위해 외부로 나가는 친구 차단/삭제 요청 에러는 FRIENDSHIP_HIDE_REASON 통일
            friendshipService.blockFriendship(friendshipId, memberId);
        } catch (GeneralException e) {
            log.error("친구 차단 실패: {}", e.getMessage());
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_HIDE_REASON);
        }

        return ResponseEntity.ok(ApiResponseForm.success(null, "친구 신청 삭제 성공"));
    }

    @Operation(summary = "친구 신청 취소")
    @DeleteMapping("/api/friendships/{friendshipId}")
    public ResponseEntity<ApiResponseForm<Void>> cancelFriendRequest(
            @PathVariable Long friendshipId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        try { //분쟁 및 친구 목록 크롤링 방지를 위해 외부로 나가는 친구 차단/삭제 요청 에러는 FRIENDSHIP_HIDE_REASON 통일
            friendshipService.cancelFriendRequest(friendshipId, memberId);
        } catch (GeneralException e) {
            log.error("친구 신청 취소 실패: {}", e.getMessage());
            throw new GeneralException(FriendshipErrorCode.FRIENDSHIP_HIDE_REASON);
        }
        return ResponseEntity.ok(ApiResponseForm.success(null, "친구 신청 취소 성공"));
    }
}
