package unicon.Achiva.domain.push.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushSendRequest {
    private UUID targetMemberId;  // null이면 전체 발송 (관리자만 가능)

    @NotBlank(message = "title은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "body는 필수입니다")
    @Size(max = 500, message = "본문은 500자를 초과할 수 없습니다")
    private String body;

    private Map<String, Object> data;  // 선택, 앱에서 처리할 추가 데이터 (예: {"screen": "goal", "goalId": "123"})
}
