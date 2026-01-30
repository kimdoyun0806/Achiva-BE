package unicon.Achiva.domain.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PushSendResponse {
    private boolean success;
    private int sentCount;      // 성공한 전송 개수
    private int failedCount;    // 실패한 전송 개수
    private String message;     // 선택, 전체 메시지
    private List<PushResult> results;  // 전송 결과 상세

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PushResult {
        private String expoPushToken;   // 대상 토큰
        private String status;           // "ok" 또는 "error"
        private String ticketId;         // Expo가 발급한 티켓 ID (성공 시)
        private String message;          // 에러 메시지 (실패 시)
    }
}
