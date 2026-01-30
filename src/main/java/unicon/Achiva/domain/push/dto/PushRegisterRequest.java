package unicon.Achiva.domain.push.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushRegisterRequest {
    @NotBlank(message = "linkToken은 필수입니다")
    private String linkToken;

    @NotBlank(message = "expoPushToken은 필수입니다")
    @Pattern(regexp = "^ExponentPushToken\\[.+\\]$", message = "유효하지 않은 Expo 푸시 토큰 형식입니다")
    private String expoPushToken;

    @Size(max = 255, message = "디바이스 정보는 255자를 초과할 수 없습니다")
    private String deviceInfo;   // 선택 필드
}
