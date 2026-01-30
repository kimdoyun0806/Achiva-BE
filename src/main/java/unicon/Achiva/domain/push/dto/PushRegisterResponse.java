package unicon.Achiva.domain.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PushRegisterResponse {
    private boolean success;
    private String message;
}
