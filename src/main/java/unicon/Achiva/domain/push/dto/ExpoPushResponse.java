package unicon.Achiva.domain.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExpoPushResponse {
    private String status;      // "ok" 또는 "error"
    private String ticketId;    // Expo가 발급한 티켓 ID
    private String message;     // 에러 메시지 (실패 시)
}
